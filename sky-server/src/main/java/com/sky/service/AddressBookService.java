package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;
import com.sky.result.Result;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    Result<List<AddressBook>> listByUserId();

    void setDefault(AddressBook addressBook);
}
