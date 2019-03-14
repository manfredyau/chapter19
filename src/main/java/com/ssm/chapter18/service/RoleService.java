package com.ssm.chapter18.service;

import com.ssm.chapter18.pojo.Role;

import java.util.List;

public interface RoleService {
    Role getRole(Long id);

    int deleteRole(Long id);

    Role insertRole(Role role);

    Role updateRole(Role role);

    List<Role> findRoles(String roleName, String note);
}
