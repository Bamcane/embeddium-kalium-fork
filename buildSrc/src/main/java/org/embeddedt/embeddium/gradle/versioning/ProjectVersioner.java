package org.embeddedt.embeddium.gradle.versioning;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ProjectVersioner {
    public static String computeVersion(File projectDir, Map<String, ?> projectProperties) {
        String modVersion = projectProperties.get("mod_version").toString();
        String minecraftVersion = projectProperties.get("minecraft_version").toString();
        boolean isReleaseBuild = projectProperties.containsKey("build.release");
        if (isReleaseBuild) {
            return "%s+mc%s".formatted(modVersion, minecraftVersion);
        } else {
            return "%s-beta+mc%s".formatted(modVersion, minecraftVersion);
        }
    }
}
