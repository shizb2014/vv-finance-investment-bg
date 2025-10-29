package com.vv.finance.investment.bg.stock.information.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.finance.investment.bg.entity.uts.NewsHk;
import com.vv.finance.investment.bg.mapper.uts.NewsHkMapper;
import com.vv.finance.investment.bg.stock.information.service.INewsHkService;
import org.springframework.stereotype.Service;

/**
 * @ClassName NewsHkServiceImpl
 * @Deacription 新股资讯
 * @Author lh.sz
 * @Date 2021年09月14日 11:49
 **/
@Service
public class NewsHkServiceImpl extends ServiceImpl<NewsHkMapper, NewsHk> implements INewsHkService {
}
