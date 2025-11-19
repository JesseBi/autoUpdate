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
            // 1. 基础校验
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return "错误: Token 不能为空";
            }
            if (request.getGithubUrl() == null || !request.getGithubUrl().contains("github.com")) {
                return "错误: 请提供有效的 GitHub 文件链接";
            }

            // 2. 解析 GitHub URL
            // 假设 URL 格式: https://github.com/{owner}/{repo}/blob/{branch}/{path}
            // 例子: https://github.com/octocat/Hello-World/blob/master/README.md
            GitHubInfo info = parseGithubUrl(request.getGithubUrl());

            // 3. 调用 Service 修改文件
            // 注意：这里我们忽略了 branch，直接改默认分支。如果需要支持特定分支，需修改 Service
            githubService.appendLineToYaml(
                    info.getRepoName(),  // 例如 "octocat/Hello-World"
                    info.getFilePath(),  // 例如 "README.md"
                    request.getLineToAdd(),
                    request.getToken()
            );

            return "成功: 代码已添加到 " + info.getFilePath();

        } catch (Exception e) {
            e.printStackTrace();
            return "失败: " + e.getMessage();
        }
    }

    // --- 辅助方法：解析 URL ---
    private GitHubInfo parseGithubUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        String path = url.getPath(); // 得到 "/user/repo/blob/branch/path/to/file"

        // 去掉开头的 "/"
        if (path.startsWith("/")) path = path.substring(1);

        String[] parts = path.split("/", 5); // 分割成：[user, repo, blob, branch, remaining_path]

        if (parts.length < 5) {
            throw new IllegalArgumentException("无法解析 URL，格式似乎不对？");
        }

        String owner = parts[0];
        String repo = parts[1];
        // parts[2] 是 "blob" (如果是文件查看页)
        // parts[3] 是 branch (例如 main)
        String filePath = parts[4]; // 剩下的部分全是文件路径

        return new GitHubInfo(owner + "/" + repo, filePath);
    }

    // 简单的内部类用于存解析结果
    @Data
    static class GitHubInfo {
        private final String repoName;
        private final String filePath;
    }

    // 接收前端请求的 DTO
    @Data
    public static class ConfigRequest {
        private String githubUrl; // 前端传来的直接是 GitHub Link
        private String lineToAdd;
        private String token;
    }
}
