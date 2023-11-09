package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
    /**
     * C端根据用户Id获取地址簿信息
     * @return
     */
    @Override
    public Result<List<AddressBook>> listByUserId() {
        Long userId = BaseContext.getCurrentId();

        List<AddressBook> addressBookList = list(new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, userId));


        return Result.success(addressBookList);
    }

    @Transactional
    @Override
    public void setDefault(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();

        Long id = addressBook.getId();

        update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId,userId)
                .set(AddressBook::getIsDefault,0));

        update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId,userId)
                .eq(AddressBook::getId,id)
                .set(AddressBook::getIsDefault,1));

    }
}
