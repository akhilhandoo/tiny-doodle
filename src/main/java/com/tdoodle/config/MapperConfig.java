package com.tdoodle.config;

import org.mapstruct.InjectionStrategy;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@org.mapstruct.MapperConfig(componentModel = SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public class MapperConfig {}
