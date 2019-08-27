import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author jiang
 */
public class DelLastUpdated {

    private static PropertyHelper propHelper = new PropertyHelper("config");
    private static final String KEY_MAVEN_REPO = "maven.repo";
    private static final String MAVEN_REPO_PATH = propHelper.getValue(KEY_MAVEN_REPO);
    private static final String FILE_SUFFIX = "lastUpdated";
    private static final Log log = LogFactory.getLog(DelLastUpdated.class);

    public static void main(String[] args) {
        File mavenRep = new File(MAVEN_REPO_PATH);
        if (!mavenRep.exists()) {
            log.warn("Maven仓库不存在！");
            return;
        }
        File[] files = mavenRep.listFiles((FilenameFilter) FileFilterUtils
                .directoryFileFilter());
        delFileRecr(files, null);
        log.info("清除lastUpdated文件结束！");
    }

    private static void delFileRecr(File[] dirs, File[] files) {
        if (dirs != null && dirs.length > 0) {
            for ( File dir : dirs ) {
                File[] childDir = dir.listFiles((FilenameFilter) FileFilterUtils
                        .directoryFileFilter());
                File[] childFiles = dir.listFiles((FilenameFilter) FileFilterUtils
                        .suffixFileFilter(FILE_SUFFIX));
                delFileRecr(childDir, childFiles);
            }
        }
        if (files != null && files.length > 0) {
            for ( File file : files ) {
                if (file.delete()) {
                    log.info("文件: [" + file.getName() + "] 已经被删除！");
                }
            }
        }
    }
}
