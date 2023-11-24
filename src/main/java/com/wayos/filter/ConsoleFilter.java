package com.wayos.filter;

import javax.servlet.annotation.WebFilter;

@WebFilter("/console/*")
public class ConsoleFilter extends AuthorizationFilter {}
