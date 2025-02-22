package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.models.auth.In;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.security.provider.MD5;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public Result<String> save(EmployeeDTO employeeDTO) {

        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO,employee);

//        LocalDateTime now = LocalDateTime.now();
//
//        employee.setCreateTime(now);
//        employee.setUpdateTime(now);

        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));


//        Long adminId = AdminHolder.getAdminId();

//        Long currentId = BaseContext.getCurrentId();

//        employee.setCreateUser(currentId);
//        employee.setUpdateUser(currentId);

        boolean flag = save(employee);



//        employeeMapper.insert(employee);

        return Result.success();
    }

    @Override
    public Result<PageResult> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        int currentPage = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        String name = employeePageQueryDTO.getName();

        Page<Employee> pageParam = new Page<>(currentPage,pageSize);

//        if (name != null && !"".equals(name)){
//            Page<Employee> page = page(pageParam, new LambdaQueryWrapper<Employee>().like(Employee::getName, name));
//            PageResult result = new PageResult(page.getTotal(),page.getRecords());
//            return Result.success(result);
//        }


        @SuppressWarnings("unchecked")
        Page<Employee> page = page(pageParam,
                new LambdaQueryWrapper<Employee>()
                        .eq((name != null && !"".equals(name)),Employee::getName,name)
                        .orderByDesc(Employee::getUpdateTime));

        PageResult result = new PageResult(page.getTotal(), page.getRecords());

        return Result.success(result);
    }

    @Override
    public Result<String> statusChange(Integer status, Long id) {

//        Long userId = BaseContext.getCurrentId();

//        boolean update = update(new LambdaUpdateWrapper<Employee>()
//                .eq(Employee::getId, id)
//                .set(Employee::getStatus, status)
//                .set(Employee::getUpdateTime,LocalDateTime.now())
//                .set(Employee::getUpdateUser,userId));

//        boolean update = update(new LambdaUpdateWrapper<Employee>()
//                .eq(Employee::getId, id)
//                .set(Employee::getStatus, status));

        Employee employee = Employee.builder().id(id)
                .status(status)
                .build();

        boolean update = updateById(employee);


        if (!update){
            return Result.error("修改失败");
        }

        return Result.success();
    }


}
