package com.itheima.test;

import com.hankcs.lucene.HanLPAnalyzer;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * lucene 测试 联系
 *
 * @author ymeng
 */
public class MainApp {

    /**
     * 将数据写入索引库
     *
     * @throws IOException
     */
    @Test
    public void indexWriter() throws IOException {
        //打开索引库位置
        Directory d = FSDirectory.open(Paths.get("C:\\mySoftware\\lucene\\index"));
        //配置分词器
        IndexWriterConfig conf = new IndexWriterConfig(new HanLPAnalyzer());
        //索引编写器
        IndexWriter indexWriter = new IndexWriter(d, conf);
        //读取文档资料
        File file = new File("C:\\mySoftware\\lucene\\source");
        //获取数据源
        File[] files = file.listFiles();
        //遍历文件数组
        for (File f : files) {
            //文件标题
            String name = f.getName();
            //文件内容
            String fileToString = FileUtils.readFileToString(f);
            //文件路径
            String path = f.getPath();
            //文件大小
            long sizeOf = FileUtils.sizeOf(f);

            //创建域
            TextField fname = new TextField("fname", name, Field.Store.YES);
            TextField fcontent = new TextField("fcontent", fileToString, Field.Store.YES);
            StoredField fpath = new StoredField("fpath", path);
            LongPoint fsize = new LongPoint("fsize", sizeOf);

            //创建文档
            Document document = new Document();
            document.add(fname);
            document.add(fcontent);
            document.add(fpath);
            document.add(fsize);
            //把文档写入到索引库
            indexWriter.addDocument(document);
        }
        indexWriter.close();

    }

    /**
     * 读取索引库
     */
    @Test
    public void indexReader() throws IOException, ParseException {
        //打开索引库
        Directory directory = FSDirectory.open(Paths.get("C:\\mySoftware\\lucene\\index"));
        //索引读取器
        IndexReader indexReader = DirectoryReader.open(directory);
        //二次包装
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //查询所有查询器
        Query query = new MatchAllDocsQuery();
        //不分词单域查询
        Query query1 = new TermQuery(new Term("fname","中国人"));
        //范围查询
        Query query2 = LongPoint.newRangeQuery("fsize",10,100);
        //不分词组合查询
        BooleanClause booleanClause = new BooleanClause(query,BooleanClause.Occur.MUST);
        BooleanClause booleanClause1 = new BooleanClause(query1,BooleanClause.Occur.MUST_NOT);
        Query query3 = new BooleanQuery.Builder().add(booleanClause).add(booleanClause1).build();

        //分词单域查询
        QueryParser queryParser = new QueryParser("fname",new HanLPAnalyzer());
        Query query4 = queryParser.parse("中国人聪明");
        //分词多域查询
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{"fname","fcontent"},new HanLPAnalyzer());
        Query query5 = multiFieldQueryParser.parse("中国人");

        //查询索引库  search 只能查询文档数量 和 文档编号
        TopDocs search = indexSearcher.search(query5, 10);
        System.out.println("文档数量为------>"+search.totalHits);
        System.out.println();
        System.out.println();
        System.out.println("---------------------------------------华丽的分割线---------------------------------------------");
        //遍历文档编号集合
        for (ScoreDoc scoreDoc : search.scoreDocs) {
            //查询文档编号
            int i = scoreDoc.doc;
            System.out.println("文档编号为---->"+i);
            //根据文档编号 查询文档内容
            Document doc = indexSearcher.doc(i);
            String fname = doc.get("fname");
            System.out.println("文档标题为----->"+fname);
            String fcontent = doc.get("fcontent");
            System.out.println("文档内容为----->"+fcontent);
            String fpath = doc.get("fpath");
            System.out.println("文档路径为----->"+fpath);
            String fsize = doc.get("fsize");
            System.out.println("文档大小为----->"+fsize);
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("---------------------------------------华丽的分割线---------------------------------------------");


        }
        indexReader.close();

    }

}
