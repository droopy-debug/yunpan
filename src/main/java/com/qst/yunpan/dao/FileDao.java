package com.qst.yunpan.dao;

import com.qst.yunpan.pojo.RecycleFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileDao {
    void insertFiles(@Param("filePath") String filePath, @Param("userName") String userName) throws Exception;
    //查询回收站中数据
    List<RecycleFile> selectFiles(@Param("userName") String userName) throws Exception;

    RecycleFile selectFile(@Param("fileId") int fileId) throws Exception;
    void deleteFile(@Param("fileId") int fileId, @Param("userName") String userName) throws Exception;
    void delete(@Param("filePath") String filepath, @Param("userName") String userName) throws  Exception;

    void deleteFiles(@Param("userName") String userName) throws Exception;
}
