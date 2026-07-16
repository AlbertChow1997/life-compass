package com.albertchow.lifecompass.shop;

import com.albertchow.lifecompass.common.Result;
import com.albertchow.lifecompass.entity.ShopType;
import com.albertchow.lifecompass.mapper.ShopTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public list of business categories (requirement 5's filter facets). */
@RestController
@RequestMapping("/api/shop-type")
@RequiredArgsConstructor
public class ShopTypeController {

    private final ShopTypeMapper shopTypeMapper;

    @GetMapping
    public Result<List<ShopType>> list() {
        var query = new LambdaQueryWrapper<ShopType>().orderByAsc(ShopType::getSort);
        return Result.ok(shopTypeMapper.selectList(query));
    }
}
