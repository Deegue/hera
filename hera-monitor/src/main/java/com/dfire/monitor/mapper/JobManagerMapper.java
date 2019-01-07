package com.dfire.monitor.mapper;

import com.dfire.monitor.domain.ActionTime;
import com.dfire.monitor.domain.JobHistoryVo;
import com.dfire.monitor.domain.JobStatusNum;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 下午3:05 2018/8/14
 * @desc
 */
public interface JobManagerMapper {

    /**
     * 今日任务详情
     *
     * @param status
     * @return
     */

    @Select("select " +
            " his.job_id,job.name as job_name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator,count(*) as times " +
            " from" +
            " (select job_id,start_time start_time,end_time,execute_host,status,operator from hera_action_history " +
            " where left(start_time,10) = CURRENT_DATE() and status = #{status,jdbcType=VARCHAR}) his " +
            " left join hera_job job on his.job_id = job.id" +
            " group by his.job_id,job.name,job.description,his.start_time,his.end_time,his.execute_host,his.status,his.operator" +
            " order by job_id")
    List<JobHistoryVo> findAllJobHistoryByStatus(String status);


    /**
     * 任务运行时长top10
     *
     * @param map
     * @return
     */
    @Select("select a.job_id,a.action_id,a.job_time,b.run_type from" +
            "        (select job_id,action_id,timestampdiff(SECOND,start_time,end_time)/60 as job_time from hera_action_history where start_time>#{startDate}" +
            "        " +
            "        and end_time<#{endDate} ) a" +
            "        " +
            "        inner join (select id,run_type from hera_job ) b" +
            "        on a.job_id = b.id order by a.job_time desc limit #{limitNum};")
    List<ActionTime> findJobRunTimeTop10(Map<String, Object> map);

    /**
     * 任务昨日运行时长
     *
     * @param jobId
     * @param startDate
     * @return
     */
    @Select(" select max(timestampdiff(SECOND,start_time,end_time)/60) from hera_action_history" +
            "        WHERE  job_id = #{jobId}" +
            "        AND DATE_FORMAT(start_time, '%Y-%m-%d') =  DATE_FORMAT(#{startDate}, '%Y-%m-%d')")
    Integer getYesterdayRunTime(@Param("jobId") Integer jobId, @Param("startDate") String startDate);

    /**
     * 按照运行状态汇总,初始化首页饼图
     *
     * @return
     */
    @Select(" select status,count(1) as num" +
            "        from" +
            "        (" +
            "        select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status" +
            "        from hera_action_history" +
            "        where left(start_time,10)=CURRENT_DATE()" +
            "        group by job_id" +
            "        ) t" +
            "        group by status")
    List<JobStatusNum> findAllJobStatus();


    /**
     * 按照日期查询任务明细
     *
     * @param curDate
     * @return
     */
    @Select("select status,count(1) as num " +
            "        from " +
            "        ( " +
            "        select job_id,substring_index(group_concat(status order by start_time desc),\",\",1) as status " +
            "        from hera_action_history " +
            "        where left(start_time,10)=#{selectDate,jdbcType=VARCHAR} " +
            "        group by job_id " +
            "        ) t " +
            "        group by status")
    List<JobStatusNum> findJobDetailByDate(String curDate);

    /**
     * 按照status查询任务明细
     *
     * @param status
     * @return
     */
    @Select(" select count(1) num ,status, LEFT(start_time,10) curDate " +
            "        from  hera_action_history " +
            "        where " +
            "        DATE_SUB(CURDATE(), INTERVAL 5 DAY) <= start_time " +
            "        and status = #{status,jdbcType=VARCHAR} " +
            "        GROUP BY LEFT(start_time,10)")
    List<JobStatusNum> findJobDetailByStatus(String status);
}
