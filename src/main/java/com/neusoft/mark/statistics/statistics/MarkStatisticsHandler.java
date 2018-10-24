package com.neusoft.mark.statistics.statistics;
import com.neusipo.TrsResult;
import com.neusipo.controller.SortController;
import com.neusipo.domain.SortData;
import com.neusipo.domain.SortResult;
import com.neusipo.utils.ConPool;
import com.neusoft.jszk.statistics.SortStatistics;
import com.neusoft.mark.statistics.statistics.utils.DBUtil;
import com.neusoft.mark.statistics.statistics.utils.IDGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * 统计demo
 */
public class MarkStatisticsHandler {
    /**
     * 统计方法业务逻辑
     */
    public static void validate() {
        //1. 获取数据库连接
        Connection conn= DBUtil.getConnection();
        SortController sortController = new SortController();
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            //2. 查询标引词构成的检索式
            String sql="select * from sipo_mp_ti_mark";
            ps= conn.prepareStatement(sql);
            rs=ps.executeQuery();
            //2.1 遍历2 结果集
            while(rs.next()){
                         System.out.println("test");
                        String an=rs.getString(4);
                        String word=rs.getString(5);
                        String citedAn=rs.getString(7);
                        TrsResult trsResult = new TrsResult();
                        System.out.println("test11");
                        //3. 拿标引词构成的检索式到trs检索，获取文献结果集
                        List<Map<String,Object>> resSearchResult=trsResult.getTRSResult(word);

                        String insertsql="";
                        String id= IDGenerator.generate();
                        //4. 将文献结果集（an,ipc）调用统计服务接口，返回出现频次最多的前十ipc分类号
                        Map<String,Integer> esSearchResult= SortStatistics.getIpcSort(resSearchResult);
                        System.out.println(esSearchResult);
                        //4.1 遍历统计服务返回的结果集，拼接ic检索式
                        String ipckeyword=" IC=(";
                        int count=1;
                        for (Map.Entry<String, Integer> entry : esSearchResult.entrySet()) {
                            System.out.println(entry.getKey() + ":" + entry.getValue());
                            if(count<10){
                                if(entry.getKey().length()!=0){
                                    ipckeyword +="'"+ entry.getKey()+"'"+" or ";
                                    count++;
                                }else{
                                    continue;
                                }
                            }else if(count ==10){
                                ipckeyword +="'" +entry.getKey()+"'"+" ) ";
                                count++;
                            }else if(count>10){
                                break;
                            }
                        }
                        //5. 构建关键词和ic最终的检索式
                        String keyword=word+"and"+ipckeyword;
                        System.out.println(keyword);
                        //6. 拿5.构建的检索式再次到trs检索;如果结果集为0 和大于1000的直接返回，不调用排序服务；只有结果集在0-1000之间的才调用排序服务
                        List<Map<String,Object>> resSearchResultSecond=trsResult.getTRSResult(keyword);
                        System.out.println("1212");
                        if(resSearchResultSecond.size() ==0 ||resSearchResultSecond.size()>1000){
                            continue;
                        }else{
                            //7. 构建排序服务需要的an号格式：案卷an 以 OR 拼接，如：CN201710661169 OR CN200920042553 OR CN201611064601
                            String queryAns="";
                             for(int j=0;j<resSearchResultSecond.size();j++){
                                 Map<String,Object>  map=resSearchResultSecond.get(j);
                                 queryAns += (String) map.get("an")+" or ";
                                 if(j==resSearchResultSecond.size()-1){
                                     queryAns += (String) map.get("an");
                                 }
                             }
                            //8. 调排序接口：第一个参数为7步骤构建的an号集，第二个参数为本案卷申请号，第三个参数为对比文献申请号
                            SortResult sortResult=null;
                            try {
                                sortResult = sortController.getSortResult(queryAns, an, citedAn);
                                System.out.println("aaa111");
                            }catch (Exception e) {
                                System.out.println("aaa222");
                                continue;
                            }
                            //9. 遍历排序结果集，插入数据库
                            List<SortData> sortListData=sortResult.getSortDataList();
                            int location_cited=sortResult.getLocation_com();
                            for(int i=0;i<sortListData.size();i++){
                                SortData doc=sortListData.get(i);
                                String sortDocAn=doc.getAn();
                                String sortDocTi=doc.getTi();
                                String sortPd=doc.getPd();
                                int sortScore=doc.getScore();
                                insertsql="insert into sipo_markvalidatestatistics(id,an,cited_an,word,location_cited,sort_an,sort_ti,sort_pd,sort_score) values ('"+id+"','"+an+"','"+citedAn+"','"+word+"','"+location_cited+"','"+sortDocAn+"','"+sortDocTi+"','"+sortPd+"','"+sortScore+"')";
                                ps=conn.prepareStatement(insertsql);
                                int a=ps.executeUpdate();
                            }
                            int jszkOfflineUpdateCount=ps.executeUpdate();
                        }
                }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //10. 关闭连接
            //10.1 关闭trs连接
            ConPool.close();
            //10.2 关闭数据库连接
            DBUtil.release(conn, ps, rs);
        }
    }

    /**
     *  启动入口方法
     * @param args
     */
    public static void main(String[] args){
        MarkStatisticsHandler.validate();
    }


}
