package com.limuzi.limuziaicodemother.service;

import com.limuzi.limuziaicodemother.model.dto.app.AppQueryRequest;
import com.limuzi.limuziaicodemother.model.entity.App;
import com.limuzi.limuziaicodemother.model.entity.User;
import com.limuzi.limuziaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author limuzi
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    List<AppVO> getAppVOList(List<App> appList);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);

    void deleteFile(App oldApp);
}
