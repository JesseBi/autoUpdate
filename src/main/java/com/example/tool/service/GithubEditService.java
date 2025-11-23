package com.example.tool.service;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
// 如果你需要代理，记得保留代理的代码
// import java.net.Proxy;
// import java.net.InetSocketAddress;

@Service
public class GithubEditService {

    public void appendLineToYaml(String apiUrl, String repoName, String filePath, String lineToAdd, String token) throws IOException {

        // 如果之前配置了代理 (Proxy)，这里记得加回去: .withProxy(proxy)

        // ⚠️ 关键修改：使用 .withEndpoint() 指定公司 GitHub 地址
        GitHub github = new GitHubBuilder()
                .withOAuthToken(token)
                .withEndpoint(apiUrl) // <--- 这里连接 https://alm-github.../api/v3
                .build();

        // 验证连接 (可选，方便调试)
        if (!github.isCredentialValid()) {
            throw new IllegalStateException("Token 无效或无法连接到公司 GitHub API");
        }

        GHRepository repo = github.getRepository(repoName);
        GHContent fileContent = repo.getFileContent(filePath);

        String currentContent = fileContent.getContent();
        String newContent = currentContent + "\n" + lineToAdd;

        fileContent.update(newContent, "Tool Update: Added config via Web Tool");
    }
}
