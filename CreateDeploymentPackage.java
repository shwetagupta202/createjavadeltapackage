package com.gehcmagicvalu.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class CreateDeploymentPackage {


       private static void createFiles(Path sourceFile, Path targetFile)
                    throws IOException

       {
             if (sourceFile.toFile().exists()) {
                    System.out.println(String.format("Copying from %s to %s",
                                 sourceFile.toString(), targetFile.toString()));
                    Files.createDirectories(targetFile.getParent());
                    System.out.println("Target Direectory created");
                    Files.copy(sourceFile, targetFile,
                                 StandardCopyOption.REPLACE_EXISTING);
             }
       }

       private static void iterateGitLogsAndGenerateBuildPackage(File logFile, String basePath, String targetPath) throws IOException {

             BufferedReader br = new BufferedReader(new FileReader(logFile));
             String line = br.readLine();
             while (line != null) {
                    line = line.trim();
                    if (!"".equalsIgnoreCase(line)) {
                           Path sourceFile = Paths.get(basePath, line);
                           Path targetFile = null;

                           if (line.contains("sources/core/schema/classic-tvc-installerNew")) {
                                 /*
                                 * Copy under classic-tvc-installer
                                 */
                                 line = line.replace(
                                               "sources/core/schema/classic-tvc-installerNew", "");
                                 targetFile = Paths.get(targetPath,
                                               "classic-tvc-installerNew",
                                               line);
                                 createFiles(sourceFile, targetFile);
                           } else if (line.contains("sources/core/src/main/java")) {
                                 /*
                                 * Java files copy .class fies from target to
                                 * enovia/WEB-INF/classes
                                 */
                                 line = line.replace("sources/core/src/main/java", "");
                                 Path sourceFileDir = Paths.get(basePath,
                                               "sources\\core\\target\\classes", line).getParent();
                                 targetFile = Paths.get(targetPath, "3dspace", "WEB-INF",
                                         "classes", line);
                          	   createFiles(sourceFile, targetFile);
                                 final Path targetFilePath = Paths.get(targetPath, "3dspace",
                                               "WEB-INF", "classes", line).getParent();
                                 /*
                                 * We need to look for inner classes that are like
                                 * FNDataHandler.class and
                                 * FNDataHandler$GEHCFNCellEditor.class
                                 */
                                 final String fileName = line.substring(
                                               line.lastIndexOf("/") + 1, line.lastIndexOf("."));
                                 if (Files.exists(sourceFileDir, LinkOption.NOFOLLOW_LINKS)) {
                                 Files.walkFileTree(sourceFileDir,
                                               new SimpleFileVisitor<Path>() {
                                                     @Override
                                                     public FileVisitResult visitFile(Path file,
                                                                   BasicFileAttributes attrs)
                                                                   throws IOException {
                                                            String sourceFileName = file.toFile()
                                                                          .getName();

                                                            if (sourceFileName.startsWith(fileName)) {
                                                                   createFiles(file, Paths.get(
                                                                                targetFilePath.toString(),
                                                                                sourceFileName));

                                                            }
                                                            return FileVisitResult.CONTINUE;
                                                     }
                                               });
                                 }

                           } else if (line.contains("sources/core/src/main/resources")) {
                                 /*
                                 * Actions.xml and other resources copy under
                                 * enovia/WEB-INF/classes
                                 */
                                 line = line.replace("sources/core/src/main/resources", "");
                                 targetFile = Paths.get(targetPath, "3dspace", "WEB-INF",
                                               "classes", line);
                                 createFiles(sourceFile, targetFile);
                           } else if (line.contains("sources/core/web/WEB-INF")) {
                                 /*
                                 * Copy under the enovia/WEB-INF
                                 */
                                 line = line.replace("sources/core/web/WEB-INF", "");
                                 targetFile = Paths.get(targetPath, "3dspace", "WEB-INF",
                                               line);
                                 createFiles(sourceFile, targetFile);
                           } else if (line.contains("sources/core/web")) {
                                 /*
                                 * Copy under enovia/
                                 */
                                 line = line.replace("sources/core/web", "");
                                 targetFile = Paths.get(targetPath, "3dspace", line);
                                 createFiles(sourceFile, targetFile);
                           } else {
                                 /*
                                 * These are missing files
                                 */
                                 System.out.println("####### MISSING ######" + line);
                                 targetFile = Paths.get(targetPath, "error", line);
                                 createFiles(sourceFile, targetFile);
                           }

                    }
                    line = br.readLine();
             }

       }

       public static void main(String... args) throws IOException {
             String basePath = "D:\\projects\\GEHC-MAGIC-14x\\";
             String targetPath = "D:\\Temp\\March\\Delivery_2020_03_18_Defect";

             File logFile = new File(basePath, "sources\\logs.txt");
             System.out.println(String.format("Log file %s exists %s",
                           logFile.toString(), logFile.exists()));

             if (logFile.exists()) {
                    iterateGitLogsAndGenerateBuildPackage(logFile,basePath,targetPath);
             }

       }
}
