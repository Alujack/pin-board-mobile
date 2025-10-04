package kh.edu.rupp.fe.ite.pinboard.feature.auth.data.mapper

// Local and remote data models
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.local.UserEntity
import kh.edu.rupp.fe.ite.pinboard.feature.auth.data.remote.UserDto

// Import the missing domain model
import kh.edu.rupp.fe.ite.pinboard.feature.auth.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email
    )
}

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        name = name,
        username = username,
        email = email
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        username = username,
        email = email
    )
}
