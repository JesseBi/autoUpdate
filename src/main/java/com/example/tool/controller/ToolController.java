package com.example.tool.controller;

import com.example.tool.service.GithubEditService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RestController
@RequestMapping("/api")
public class ToolController {

    @Autowired
    private GithubEditService githubService;

    @PostMapping("/add-config")
    public String addConfig(@RequestBody ConfigRequest request) {
        try {
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return "错误: Token 不能为空";
            }

            // 1. 删除旧的 "github.com" 检查，改为检查是否包含 "/blob/" (文件查看的标准路径)
            if (request.getGithubUrl() == null || !request.getGithubUrl().contains("/blob/")) {
                return "错误: URL 看起来不对，必须包含 '/blob/'";
            }

            // 2. 解析 URL
            GitHubInfo info = parseGithubUrl(request.getGithubUrl());

            // 3. 调用 Service
            githubService.appendLineToYaml(
                    info.getApiUrl(),    // 传入计算出的 API 地址
                    info.getRepoName(),
                    info.getFilePath(),
                    request.getLineToAdd(),
                    request.getToken()
            );

            return "成功: 代码已添加到 " + info.getFilePath();

        } catch (Exception e) {
            e.printStackTrace();
            return "失败: " + e.getMessage();
        }
    }

    private GitHubInfo parseGithubUrl(String urlString) throws Exception {
        URL url = new URL(urlString);

        // A. 获取主机名 (例如 alm-github.systems.uk.hsbc)
        String host = url.getHost();

        // B. 计算 API 地址
        // 如果是 github.com，API 是 https://api.github.com
        // 如果是公司版，API 通常是 https://{host}/api/v3
        String apiUrl;
        if ("github.com".equals(host)) {
            apiUrl = "https://api.github.com";
        } else {
            apiUrl = "https://" + host + "/api/v3";
        }

        // C. 解析路径 /user/repo/blob/branch/path...
        String path = url.getPath();
        if (path.startsWith("/")) path = path.substring(1);

        // 分割: [0]user, [1]repo, [2]blob, [3]branch, [4]file...
        String[] parts = path.split("/", 5);

        if (parts.length < 5) {
            throw new IllegalArgumentException("URL 格式无法解析，请确保是具体文件的链接");
        }

        String repoName = parts[0] + "/" + parts[1];
        String filePath = parts[4];

        return new GitHubInfo(apiUrl, repoName, filePath);
    }

    @Data
    static class GitHubInfo {
        private final String apiUrl;  // 新增字段
        private final String repoName;
        private final String filePath;
    }

    @Data
    public static class ConfigRequest {
        private String githubUrl;
        private String lineToAdd;
        private String token;
    }
}
