
import java.io.*;

/**
 * Jiang
 * 2018/5/3 16:21
 * <p>
 * Description：递归方式，扫描并删除磁盘中的空文件夹
 * （C盘中的一些空文件夹是系统文件夹，删除多次后空文件夹数量不变，则停止此程序）
 */
public class DelEmptyFolders {
    //空文件夹的绝对路径
    private static StringBuffer paths;
    //本次扫描的空文件夹的数量
    private static int cnt;

    public static void main(String[] args) {
        boolean flag = true;
        do {
            cnt = 0;
            paths = new StringBuffer();
            long start = System.currentTimeMillis();
            System.out.println("正在扫描......");
            //要扫描的磁盘
            File disk = new File("/Users/JiangPeng/.m2/repository");
            //日志文件的位置
            File log = new File("/Users/JiangPeng/Desktop/log/");
            try {
                //扫描磁盘
                scanEmptyFolders(disk);
                //空文件夹数大于0时，将文件夹的绝对路径记录到日志中并再扫描一次；否则停止扫描
                if (cnt > 0) {
                    fileWrite(paths.toString(), log);
                } else {
                    flag = false;
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println("本次扫描完毕，耗时：" + (end - start) / 1000 + " 秒，共删除：" + cnt + " 个空文件夹！\n");
        } while (flag);
    }

    private static void scanEmptyFolders(File file) throws UnsupportedEncodingException {
        if (file != null && file.isDirectory()) {
            File[] files = file.listFiles();
            //非空文件夹
            if (files != null) {
                if (files.length > 0) {
                    for ( File temp : files ) {
                        scanEmptyFolders(temp);
                    }
                } else {
                    System.out.println(file.getAbsolutePath());
                    //记录日志
                    paths.append(new String((file.getAbsolutePath() + "\r").getBytes(), "UTF-8"));
                    cnt++;
                    //删除空文件夹
                    file.delete();
                }
            }
        }
    }

    private static void fileWrite(String info, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(info.getBytes());
        bos.flush();
        bos.close();
        fos.close();
    }
}
