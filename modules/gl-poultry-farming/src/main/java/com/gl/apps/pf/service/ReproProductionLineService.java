/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gl.apps.pf.service;

public interface ReproProductionLineService {

  String find(
      String lineNumber,
      Integer numberOfHeTrays,
      Integer numberOfHe,
      Integer numberOfDETrays,
      Integer numberOfDE,
      Integer numberOfDYETrays,
      Integer numberOfDYE,
      Integer numberOfRE);

  String findLineNumber(String lineNumber);

  String findNumberOfHeTrays(Integer numberOfHeTrays);

  String findNumberOfHe(Integer numberOfHe);

  String findNumberOfDETrays(Integer numberOfDETrays);

  String findNumberOfDE(Integer numberOfDE);

  String findNumberOfDYETrays(Integer numberOfDYETrays);

  String findNumberOfDYE(Integer numberOfDYE);

  String findNumberOfRE(Integer numberOfRE);
}
