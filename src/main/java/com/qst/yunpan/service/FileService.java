package com.qst.yunpan.service;

import com.qst.yunpan.dao.FileDao;
import com.qst.yunpan.dao.OfficeDao;
import com.qst.yunpan.dao.UserDao;
import com.qst.yunpan.pojo.FileCustom;
import com.qst.yunpan.pojo.RecycleFile;
import com.qst.yunpan.pojo.SummaryFile;
import com.qst.yunpan.pojo.User;
import com.qst.yunpan.utils.FileUtils;
import com.qst.yunpan.utils.UserUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileService {
    @Autowired
    private OfficeDao OfficeDao;
    @Autowired
    private  FileDao fileDao;

    //文件相对前缀
    public static final String PREFIX = "WEB-INF" + File.separator + "file" + File.separator;
    //新用户注册默认文件夹
    public static final String[] DEFAULT_DIRECTORY = { "vido", "music", "source", "image", User.RECYCLE };
    @Autowired
    private UserDao userDao;
    public void addNewNameSpace(HttpServletRequest request, String namespace) {
        String fileName = getRootPath(request);
        File file = new File(fileName, namespace);
        file.mkdir();
        for (String newFileName : DEFAULT_DIRECTORY) {
            File newFile = new File(file, newFileName);
            newFile.mkdir();
        }
    }
    public String getRootPath(HttpServletRequest request) {
        String rootPath = request.getSession().getServletContext().getRealPath("/") + PREFIX;
        return rootPath;
    }

    /**
     * 获取文件路径
     *
     * @param request
     * @param fileName
     * @return
     */
//    public String getFileName(HttpServletRequest request, String fileName) {
//        fileName= fileName.replace("\\", "//");
//        if (fileName == null||fileName.equals("\\")) {
//            System.out.println(1);
//            fileName = "";
//        }
//        String username = UserUtils.getUsername(request);
//        String realpath=getRootPath(request) + username + File.separator + fileName;
//        System.out.println();System.out.println();System.out.println();
//        return realpath;
//        System.out.println("fileName before replace: " + fileName);
//        if (fileName == null) {
//            System.out.println("fileName is null");
//            fileName = "";
//        } else if (fileName.equals("\\")) {
//            System.out.println("fileName is \\");
//            fileName = "";
//        } else {
//            fileName = fileName.replace("\\", "//"); // 修正斜杠替换
//            System.out.println("fileName after replace: " + fileName);
//        }
//
//        String username = UserUtils.getUsername(request);
//        String rootPath = getRootPath(request);
//
//        // 使用 Path 类拼接路径
//        Path realpath = Paths.get(rootPath, username, fileName).normalize();
//
//        // 打印路径以便调试
//        System.out.println("Generated file path: " + realpath.toString());
//        return realpath.toString();
//    }
    public String getFileName(HttpServletRequest request, String fileName) {
        if (fileName == null) {
            fileName = "";
        } else {
            fileName = fileName.replace("\\", "/");
        }

        if (fileName.equals("\\")) {
            System.out.println(1);
            fileName = "";
        }

        String username = UserUtils.getUsername(request);
        String realpath=getRootPath(request) + username + File.separator + fileName;
        return realpath;

    }

    /**
     * 根据用户名获取文件路径
     *
     * @param request
     * @param fileName
     * @param username
     * @return
     */
    public String getFileName(HttpServletRequest request, String fileName, String username) {
        if (username == null) {
            return getFileName(request, fileName);
        }
        if (fileName == null) {
            fileName = "";
        }
        return getRootPath(request) + username + File.separator + fileName;
    }

    /**
     * 上传文件至前端页面中的当前路径
     * @param request
     * @param files
     * @param currentPath
     * @throws Exception
     */
    public void uploadFilePath(HttpServletRequest request, MultipartFile[] files, String currentPath) throws Exception {
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String filePath = getFileName(request, currentPath);
            File distFile = new File(filePath, fileName);
            if (!distFile.exists()) {
                file.transferTo(distFile);
                if ("office".equals(FileUtils.getFileType(distFile))) {
                    try {
                        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                        String documentId = FileUtils.getDocClient().createDocument(distFile, fileName, suffix).getDocumentId();
                        OfficeDao.addOffice(documentId, FileUtils.MD5(distFile));
                    } catch (Exception e) {
                    }
                }
            }
        }
        reSize(request);//reSize的定义与实现在下一个任务点完成
    }

    /**
     * 重新计算文件大小
     *
     * @param request
     */
    public void reSize(HttpServletRequest request) {
        String userName = UserUtils.getUsername(request);
        try {

            String readblesize = countFileSize(request);
            //System.out.println("public void reSize(HttpServletRequest request):readablesize"+readblesize);
            userDao.reSize(userName, readblesize);
//            // 统计文件大小
//            long totalSize = countFileSize(new File(getFileName(request, userName)));
//            // 将大小转换为适当的单位（例如 MB, GB 等）
//            String readableSize = FileUtils.getDataSize(totalSize);
//            // 更新数据库
//            System.out.println();
//            System.out.println(" public void reSize(HttpServletRequest request) |capacity:" + readableSize);
//            userDao.reSize(userName, readableSize);
        } catch (Exception e) {
            e.printStackTrace();
            //logger.error("Failed to resize user files for user: " + userName, e);
        }
    }

    /**
     * 统计用户文件大小
     *
     * @param request
     * @return
     */
    public String countFileSize(HttpServletRequest request) {
        long countFileSize = countFileSize(new File(getFileName(request, null)));
        //System.out.println("public String countFileSize |countSize::" + countFileSize);
        return FileUtils.getDataSize(countFileSize);

//        String userName = UserUtils.getUsername(request);
//        String filePath = getFileName(request, userName);
//        File userDirectory = new File(filePath);
//
//
//        long totalFileSize = countFileSize(userDirectory);
//        return FileUtils.getDataSize(totalFileSize);
    }

    /**
     * 计算文件文件大小
     * @param srcFile
     * @return
     */
    private long countFileSize(File srcFile) {
        File[] listFiles = srcFile.listFiles();
        if (listFiles == null) {
            //System.out.println("private long countFileSize : " + "ListFIles == NULL");
            return 0;
        }
        long count = 0;
        for (File file : listFiles) {
            if (file.isDirectory()) {
                count += countFileSize(file);
            } else {
                count += file.length();
            }
        }
        System.out.println("private long countFileSize ||count:" + count);
        return count;
    }

    /**
     * 文件列表
     * @param realPath
     * @return
     */
    public List<FileCustom> listFile(String realPath) {
        File[] files = new File(realPath).listFiles();
        List<FileCustom> lists = new ArrayList<FileCustom>();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals(User.RECYCLE)) {
                    FileCustom custom = new FileCustom();
                    custom.setFileName(file.getName());
                    custom.setLastTime(FileUtils.formatTime(file.lastModified()));
                    custom.setCurrentPath(realPath);
                    if (file.isDirectory()) {
                        custom.setFileSize("-");
                    } else {
                        custom.setFileSize(FileUtils.getDataSize(file.length()));
                    }
                    custom.setFileType(FileUtils.getFileType(file));
                    lists.add(custom);
                }
            }
        }
        return lists;
    }

    /**
     * 压缩文件
     *
     * @param sourcePath
     * @return
     * @throws Exception
     */
    private String packageZip(String[] sourcePath) throws Exception {
        String zipName = sourcePath[0] + (sourcePath.length == 1 ? "" : "等" + sourcePath.length + "个文件") + ".zip";
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipName));
            for (String string : sourcePath) {
                writeZos(new File(string), "", zos);
            }
        } finally {
            if (zos != null) {
                zos.close();
            }
        }
        return zipName;
    }

    /**
     * 判断用户是单一文件下载还是多个文件下载
     * @param request
     * @param currentPath
     * @param fileNames
     * @param username
     * @return
     * @throws Exception
     */
    public File downPackage(HttpServletRequest request, String currentPath, String[] fileNames, String username) throws Exception {
        File downloadFile = null;
        if (currentPath == null) {
            currentPath = "";
        }
        //单文件length为1
        if (fileNames.length == 1) {
            downloadFile = new File(getFileName(request, currentPath, username), fileNames[0]);
            if (downloadFile.isFile()) {
                return downloadFile;
            }
        }
        String[] sourcePath = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            sourcePath[i] = getFileName(request, currentPath, username) + File.separator + fileNames[i];
        }
        String packageZipName = packageZip(sourcePath);
        downloadFile = new File(packageZipName);
        return downloadFile;
    }


    /**
     * 写入文件到压缩包
     *
     * @param file
     * @param basePath
     * @param zos
     //* @throws IOException
     */
    private void writeZos(File file, String basePath, ZipOutputStream zos) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles.length != 0) {
                for (File childFile : listFiles) {
                    writeZos(childFile, basePath + file.getName() + File.separator, zos);
                }
            }
        } else {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basePath + file.getName());
            zos.putNextEntry(entry);
            int count = 0;
            byte data[] = new byte[1024];
            while ((count = bis.read(data)) != -1) {
                zos.write(data, 0, count);
            }
            bis.close();
        }
    }


    /**
     * 删除压缩文件包
     *
     * @param downloadFile
     */
    public void deleteDownPackage(File downloadFile) {
        if (downloadFile.getName().endsWith("个文件.zip")) {
            downloadFile.delete();
        }
    }

    /**
     * 查找文件
     *
     * @param request
     * @param currentPath
     *            当前路径
     * @param regType
     *            文件类型
     * @return
     */
    public List<FileCustom> searchFile(HttpServletRequest request, String currentPath, String reg, String regType) {
        List<FileCustom> list = new ArrayList<>();
        matchFile(request, list, new File(getSearchFileName(request, currentPath)), reg, regType == null ? "" : regType);
        return list;
    }

    private String getSearchFileName(HttpServletRequest request, String fileName) {
        if (fileName == null||fileName.equals("\\")) {
            System.out.println(1);
            fileName = "";
        }
        String username = UserUtils.getUsername(request);
        String realpath=getRootPath(request) + username + File.separator + fileName;
        return realpath;
    }

    private void matchFile(HttpServletRequest request, List<FileCustom> list, File dirFile,
                           String reg,
                           String regType) {
        File[] listFiles = dirFile.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile()) {
                    String suffixType = FileUtils.getFileType(file);
                    if (suffixType.equals(regType) || (reg != null && file.getName().contains(reg))) {
                        FileCustom custom = new FileCustom();
                        custom.setFileName(file.getName());
                        custom.setLastTime(FileUtils.formatTime(file.lastModified()));
                        String parentPath = file.getParent();
                        String prePath = parentPath.substring(
                                parentPath.indexOf(getSearchFileName(request, null)) + getSearchFileName(request, null).length());
                        custom.setCurrentPath(File.separator + prePath);
                        if (file.isDirectory()) {
                            custom.setFileSize("-");
                        } else {
                            custom.setFileSize(FileUtils.getDataSize(file.length()));
                        }
                        custom.setFileType(FileUtils.getFileType(file));
                        list.add(custom);
                    }
                } else {
                    matchFile(request, list, file, reg, regType);
                }
            }
        }
    }
//        for (String fileName : directoryName) {
//            //拼接源文件的地址
//            String srcPath = currentPath + File.separator + fileName;
//            //根据源文件相对地址拼接 绝对路径
//            File src = new File(getFileName(request, srcPath));//即将删除的文件地址
//            File dest = new File(getRecyclePath(request));//回收站目录地址
//            //调用commons.jar包中的moveToDirectory移动文件,移至回收站目录
//            org.apache.commons.io.FileUtils.moveToDirectory(src, dest, true);
//            //保存本条删除信息
//            fileDao.insertFiles(srcPath, UserUtils.getUsername(request));
//        }
//        //重新计算文件大小
//        reSize(request);

    /**
     * 删除文件
     *
     * @param request
     * @param currentPath
     *            当前路径
     * @param directoryName
     *            文件名
     * @throws Exception
     */
    public void delDirectory(HttpServletRequest request, String currentPath, String[] directoryName) throws Exception {

        String username = UserUtils.getUsername(request);
        String rootPath = getRootPath(request);
        System.out.println("rootPath:  "+ rootPath);
        String recyclePath = rootPath + username+File.separator + "recycle"; // 假设有一个回收站目录
        System.out.println("recyclePath:  "+recyclePath);

        for (String dir : directoryName) {
            File directory = new File(rootPath +username +File.separator + currentPath +File.separator+ File.separator + dir);
            System.out.println("directory:  " + directory);
            if(directory.exists()) System.out.println("我在");
            else System.out.println("我不在");
            if (directory.exists() ) {
                File recycleDir = new File(recyclePath + File.separator );
                String uuu = currentPath + File.separator + dir;
                String okl = recyclePath + File.separator+dir;
                System.out.println("okl ; " + okl);
                Path path = Paths.get(okl);


                // 检查回收站目录是否已经存在
                if (recycleDir.exists()) {
                    System.out.println("recycleDir:   "+ recycleDir);
                    // 删除回收站中的目标目录
                    if(Files.exists(path))
                    {
                        Files.delete(path);
                        fileDao.delete(uuu,UserUtils.getUsername(request));
                    }
//                    if (recycleDir.delete())
//                        System.out.println("successful");
//                    else System.out.println("faulse");

                    System.out.println("Deleted existing directory in recycle bin: " + recycleDir.getAbsolutePath());
                }

                // 移动目录到回收站
                //directory.move(directory, recycleDir);
                org.apache.commons.io.FileUtils.moveToDirectory(directory, recycleDir, true);
                fileDao.insertFiles(uuu, UserUtils.getUsername(request));
                System.out.println("Moved directory to recycle bin: " + recycleDir.getAbsolutePath());
            } else {
                System.out.println("Directory does not exist or is not a directory: " + directory.getAbsolutePath());
                throw new FileNotFoundException("Directory not found: " + directory.getAbsolutePath());
            }
        }

    }
    public String getRecyclePath(HttpServletRequest request) {
        return getFileName(request, User.RECYCLE);
    }


    /**
     * 重命名文件
     *
     * @param request
     * @param currentPath
     * @param srcName
     * @param destName
     * @return
     */
    public boolean renameDirectory(HttpServletRequest request, String currentPath, String srcName, String destName) {
        //根据源文件名  获取  源地址
        File file = new File(getFileName(request, currentPath), srcName);
        //同上
        File descFile = new File(getFileName(request, currentPath), destName);
        return file.renameTo(descFile);//重命名
    }

    /**
     * 移动的文件列表
     *
     * @param realPath
     *            路径
     * @param number
     *            该路径下的文件数量
     * @return
     */
    public SummaryFile summarylistFile(String realPath, int number) {
        File file = new File(realPath);
        SummaryFile sF = new SummaryFile();
        List<SummaryFile> returnlist = new ArrayList<SummaryFile>();
        if (file.isDirectory()) {
            sF.setisFile(false);
            if (realPath.length() <= number) {
                sF.setfileName("yun盘");
                sF.setPath("");
            }else{
                String path = file.getPath();
                sF.setfileName(file.getName());
                //截取固定长度 的字符串，从number开始到value.length-number结束.
                sF.setPath(path.substring(number));
            }
            /* 设置抽象文件夹的包含文件集合 */
            for (File filex : file.listFiles()) {
                //获取当前文件的路径，构造该文件
                SummaryFile innersF = summarylistFile(filex.getPath(), number);
                if (!innersF.getisFile()) {
                    returnlist.add(innersF);
                }
            }
            sF.setListFile(returnlist);
            /* 设置抽象文件夹的包含文件夹个数 */
            sF.setListdiretory(returnlist.size());
        } else {
            sF.setisFile(true);
        }
        return sF;
    }


    public void copyDirectory(HttpServletRequest request, String currentPath, String[] directoryName,String targetdirectorypath) throws Exception {
        for (String srcName : directoryName) {
            File srcFile = new File(getFileName(request, currentPath), srcName);
            File targetFile = new File(getFileName(request, targetdirectorypath), srcName);
            /* 处理目标目录中存在同名文件或文件夹问题 */
            String srcname = srcName;
            String prefixname = "";
            String targetname = "";
            if (targetFile.exists()) {
                String[] srcnamesplit = srcname.split("\\)");
                if (srcnamesplit.length > 1) {
                    String intstring = srcnamesplit[0].substring(1);
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(intstring);
                    if (isNum.matches()) {
                        srcname = srcname.substring(srcnamesplit[0].length() + 1);
                    }
                }
                for (int i = 1; true; i++) {
                    prefixname = "(" + i + ")";
                    targetname = prefixname + srcname;
                    targetFile = new File(targetFile.getParent(), targetname);
                    if (!targetFile.exists()) {
                        break;
                    }
                }
                targetFile = new File(targetFile.getParent(), targetname);
            }
            /* 复制 */
            copyfile(srcFile, targetFile);
        }
    }

    /**
     * copy文件
     *
     * @param srcFile
     *            源文件
//     * @param targetFile目标文件
//     * @throws IOException
     */
    private void copyfile(File srcFile, File targetFile) throws IOException {
        if (!srcFile.isDirectory()) {
            /* 如果是文件，直接复制 */
            targetFile.createNewFile();
            FileInputStream src = (new FileInputStream(srcFile));
            FileOutputStream target = new FileOutputStream(targetFile);
            FileChannel in = src.getChannel();
            FileChannel out = target.getChannel();
            in.transferTo(0, in.size(), out);
            src.close();
            target.close();
        } else {
            /* 如果是文件夹，再遍历 */
            File[] listFiles = srcFile.listFiles();
            targetFile.mkdir();
            for (File file : listFiles) {
                File realtargetFile = new File(targetFile, file.getName());
                copyfile(file, realtargetFile);
            }
        }
    }



    /**
     * 移动文件
     *
     * @param request
     * @param currentPath
     *            当前路径
     * @param directoryName
     *            文件名
     * @param targetdirectorypath
     *            目标路径
     * @throws Exception
     */
    public void moveDirectory(HttpServletRequest request, String currentPath, String[] directoryName,String targetdirectorypath) throws Exception {
        for (String srcName : directoryName) {
            File srcFile = new File(getFileName(request, currentPath), srcName);
            File targetFile = new File(getFileName(request, targetdirectorypath), srcName);
            /* 处理目标目录中存在同名文件或文件夹问题 */
            String srcname = srcName;
            String prefixname = "";
            String targetname = "";
            if (targetFile.exists()) {
                String[] srcnamesplit = srcname.split("\\)");
                if (srcnamesplit.length > 1) {
                    String intstring = srcnamesplit[0].substring(1);
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(intstring);
                    if (isNum.matches()) {
                        srcname = srcname.substring(srcnamesplit[0].length() + 1);
                    }
                }
                for (int i = 1; true; i++) {
                    prefixname = "(" + i + ")";
                    targetname = prefixname + srcname;
                    targetFile = new File(targetFile.getParent(), targetname);
                    if (!targetFile.exists()) {
                        break;
                    }
                }
                targetFile = new File(targetFile.getParent(), targetname);
            }
            /* 移动即先复制，再删除 */
            copyfile(srcFile, targetFile);
            delFile(srcFile);
        }
    }


    /**
     * 删除文件
     *
     * @param srcFile
     *            源文件
     * @throws Exception
     */
    private void delFile(File srcFile) throws Exception {
        /* 如果是文件，直接删除 */
        if (!srcFile.isDirectory()) {
            /* 使用map 存储删除的 文件路径，同时保存用户名 */
            srcFile.delete();
            return;
        }
        /* 如果是文件夹，再遍历 */
        File[] listFiles = srcFile.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                delFile(file);
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        if (srcFile.exists()) {
            srcFile.delete();
        }
    }



    /*--回收站显示所有删除文件--*/
    public List<RecycleFile> recycleFiles(HttpServletRequest request) throws Exception {
        List<RecycleFile> recycleFiles = fileDao.selectFiles(UserUtils.getUsername(request));
        for (RecycleFile file : recycleFiles) {
            File f = new File(getRecyclePath(request), new File(file.getFilePath()).getName());
            file.setFileName(f.getName());
            file.setLastTime(FileUtils.formatTime(f.lastModified()));
        }
        return recycleFiles;
    }


    /* 还原文件 */
//难点2.还原文件时不等同于移动文件到，因为还原文件需要保存多个地址，而还原只单纯保存一个地址
//而且还原时需要判断父子文件是否都删除了，此时就需要新建立父文件，再建立子文件，而commons.io.FileUtils则可以很好的解决问题
    public void revertDirectory(HttpServletRequest request, int[] fileId) throws Exception {
        for (int id : fileId) {
            RecycleFile file = fileDao.selectFile(id);
            String fileName = new File(file.getFilePath()).getName();
            File src = new File(getRecyclePath(request), fileName);
            File dest = new File(getFileName(request, file.getFilePath()));
            org.apache.commons.io.FileUtils.moveToDirectory(src, dest.getParentFile(), true);
            fileDao.deleteFile(id, UserUtils.getUsername(request));
        }
    }


    /*--依次遍历recycle下各个文件，并逐一删除--*/
    public void delAllRecycle(HttpServletRequest request) throws Exception {
        //获取回收站中的所有文件
        File file = new File(getRecyclePath(request));
        //遍历文件夹下所有文件
        File[] inferiorFile = file.listFiles();
        for (File f : inferiorFile) {
            delFile(f);//调用本类下面的delFile()方法
        }
        //根据用户进行删除
        fileDao.deleteFiles(UserUtils.getUsername(request));
        reSize(request);
    }


    /**
     *
     * @param response
     * @param request
     * @param currentPath
     * @param fileName
     * @param type
     * @throws IOException
     */
    /*FileService类中添加respFile()方法，通过Apache的IOUtils.copy()方法对当前图片/txt文件进行读写。
    前端将为txt文档传入对应的“docum”标识；当type变量为“docum”的时，则需要再做一次编码装换，以防止文本乱码，*/
    public void respFile(HttpServletResponse response, HttpServletRequest request, String currentPath, String fileName, String type) throws IOException {
        File file = new File(getFileName(request, currentPath), fileName);
        InputStream inputStream = new FileInputStream(file);
        if ("docum".equals(type)) {
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(inputStream, response.getWriter(), "UTF-8");
        } else {
            IOUtils.copy(inputStream, response.getOutputStream());
        }
    }

    /**
     * 获取文件列表方法(android)
     * @param realPath
     * @param request
     * @param username
     * @return
     */
    public List<FileCustom> listFileForApp(String realPath,HttpServletRequest request,String username) {
        String preFix = getRootPath(request) + username + File.separator;
        //对文件操作  需要new出一个文件，代表指向该文件内存地址
        File[] files = new File(realPath).listFiles();
        List<FileCustom> lists = new ArrayList<FileCustom>();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals(User.RECYCLE)) {
                    FileCustom custom = new FileCustom();
                    custom.setFileName(file.getName());
                    custom.setLastTime(FileUtils.formatTime(file.lastModified()));
                    /* 保存文件的删除前路径以及当前路径 */
                    // custom.setFilePath(prePath);
                    custom.setCurrentPath(realPath.replace(preFix, ""));
                    if (file.isDirectory()) {
                        custom.setFileSize("-");
                        custom.setFileType("folder");
                    } else {
                        custom.setFileSize(FileUtils.getDataSize(file.length()));
                        custom.setFileType("file");
                    }
                    lists.add(custom);
                }
            }
        }
        return lists;
    }

    /**
     * 上传文件(安卓接口)
     *
     * @param request
     * @param
     *          //files
     *            文件
     * @param currentPath
     *            当前路径
     * @throws Exception
     */
    public void uploadFilePathExt(HttpServletRequest request, MultipartFile file, String currentPath,String username) throws Exception {
        String fileName = file.getOriginalFilename();
        String filePath = getFileName(request, currentPath,username);
        File distFile = new File(filePath, fileName);
        if (!distFile.exists()) {
            file.transferTo(distFile);
            if ("office".equals(FileUtils.getFileType(distFile))) {
                try {
                    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    String documentId = FileUtils.getDocClient().createDocument(distFile, fileName, suffix)
                            .getDocumentId();
                    OfficeDao.addOffice(documentId, FileUtils.MD5(distFile));
                } catch (Exception e) {
                }
            }
        }
        reSize(request);
    }


}
