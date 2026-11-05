package com.nimbus.iam.interfaces.api.controller

import com.nimbus.iam.application.service.RoleService
import com.nimbus.iam.interfaces.api.dto.CreateRoleRequest
import com.nimbus.iam.interfaces.api.dto.RoleResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/roles")
class AdminRoleController(
    private val roleService: RoleService
) {
    @GetMapping
    fun listRoles(): ResponseEntity<List<RoleResponse>> {
        val roles = roleService.findAll().map(RoleResponse::from)
        return ResponseEntity.ok(roles)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(@Valid @RequestBody request: CreateRoleRequest): RoleResponse {
        val role = roleService.create(request.toCommand())
        return RoleResponse.from(role)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRole(@PathVariable id: UUID) {
        roleService.delete(id)
    }
}
