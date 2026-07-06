package com.tdoodle.config;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.mapstruct.InjectionStrategy;

@org.mapstruct.MapperConfig(
    componentModel = SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public class MapperConfig {}
