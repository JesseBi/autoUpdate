package com.example.tool.service;

import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class GithubEditService {

    // 修改方法签名，增加 String token 参数
    public void appendLineToYaml(String repoName, String filePath, String lineToAdd, String token) throws IOException {

        // ⚠️ 关键修改：使用传入的 token 初始化 GitHub 客户端
        GitHub github = new GitHubBuilder().withOAuthToken(token).build();

        // 下面的逻辑保持不变
        GHRepository repo = github.getRepository(repoName);
        GHContent fileContent = repo.getFileContent(filePath);

        String currentContent = fileContent.getContent();
        String newContent = currentContent + "\n" + lineToAdd;

        // 提交更新
        fileContent.update(newContent, "Tool Update: Added via Web UI");

        System.out.println("成功更新文件: " + filePath);
    }
}
