import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.JarFilter;

import java.io.*;

/**
 * @author jiang
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);
    private static PropertyHelper propHelper = new PropertyHelper("config");
    private static Runtime runRuntime = Runtime.getRuntime();
    private static boolean isDelete = Boolean.parseBoolean(propHelper.getValue("delete-installed-jar"));
    private static boolean isMove = Boolean.parseBoolean(propHelper.getValue("move-installed-jar"));
    private static final String KEY_JARPATH = "jar-path";
    private static final String KEY_BACKUPPATH = "back-path";
    private static final String ENCODE = "gbk";
    private static final String INSTALL_PATH = propHelper.getValue(KEY_JARPATH);

    public static void main(String[] args) {

        log.info("The path of the jars is [" + INSTALL_PATH + "].");
        File file = new File(INSTALL_PATH);
        if (!file.isDirectory()) {
            log.warn("The path must be a directory.");
            return;
        }
        FilenameFilter filter = new JarFilter();
        File[] jarFiles = file.listFiles(filter);
        for ( File jar : jarFiles ) {
            installJarToMaven(jar);
            if (isDelete) {
                log.info("Delete the original jar file [" + jar.getName() + "].");
                jar.delete();
            } else {
                if (isMove) {
                    String backupPath = propHelper.getValue(KEY_BACKUPPATH);
                    backupJar(jar, file, backupPath);
                }
            }
        }
    }

    private static void backupJar(File jar, File file, String backupPath) {
        String CMD_BACKUP_JAR = "copy " + INSTALL_PATH + File.separator + jar.getName() + " " + backupPath;
        String[] cmds = new String[]{"cmd", "/C", CMD_BACKUP_JAR};
        try {
            Process process = runRuntime.exec(cmds, null, file);
            printResult(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("The jar [" + jar.getName() + "]  is backup, it's will be deleted.\r");
        jar.delete();
    }

    private static void installJarToMaven(File file) {
        String fileName = file.getName();
        String jarName = getJarName(fileName);
        String groupId = null;
        String artifactId = null;
        String version = null;
        int groupIndex = jarName.indexOf("-");
        if (groupIndex == -1) {
            version = artifactId = groupId = jarName;
        } else {
            groupId = jarName.substring(0, groupIndex);
            int versionIndex = jarName.lastIndexOf("-");
            if (groupIndex == versionIndex) {
                version = artifactId = jarName.substring(versionIndex + 1, jarName.length());
            } else {
                artifactId = jarName.substring(groupIndex + 1, versionIndex);
                version = jarName.substring(versionIndex + 1, jarName.length());
            }
        }
        log.info("Jar [" + jarName + "] will be installed with the groupId=" + groupId + " ,"
                + "artifactId=" + artifactId + " , version=" + version + ".");
        executeInstall(groupId, artifactId, version, file.getPath());
    }

    private static void executeInstall(String groupId, String artifactId,
                                       String version, String path) {
        String CMD_INSTALL_FILE = createInstallFileCMD(groupId, artifactId,
                version, path);
        String[] cmds = new String[]{"cmd", "/C", CMD_INSTALL_FILE};
        try {
            Process process = runRuntime.exec(cmds);
            printResult(process);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printResult(Process process) throws IOException {
        InputStream is = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, ENCODE));
        String lineStr;
        while ((lineStr = br.readLine()) != null) {
            System.out.println(lineStr);
        }
    }

    private static String createInstallFileCMD(String groupId,
                                               String artifactId, String version, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("mvn install:install-file -DgroupId=").append(groupId)
                .append(" -DartifactId=").append(artifactId)
                .append(" -Dversion=").append(version)
                .append(" -Dpackaging=jar")
                .append(" -Dfile=").append(path);
        log.debug(sb.toString());
        return sb.toString();
    }

    private static String getJarName(String fileName) {
        int index = fileName.indexOf(".jar");
        return fileName.substring(0, index);
    }
}
