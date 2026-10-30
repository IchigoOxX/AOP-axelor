/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import Service from './index';

const COUNTRY_FIELDS = [
    'id',
    'name',
    'phonePrefix',
    'alpha2Code'
];
const CITY_FIELDS = [
    'id',
    'name',
    'zip'
];
export async function fetchCountries() {
    try {
        let res = await Service.search('com.axelor.apps.base.db.Country', {fields: COUNTRY_FIELDS});
        if (res && res.data) {
            return res.data;
        }
        throw new Error('No countries found');
    } catch (error) {
        console.error('Error fetching countries:', error);
        throw error;
    }
}

export async function fetchCities(country) {
    try {
        let res = await Service.search('com.axelor.apps.base.db.City', {
            fields: CITY_FIELDS,
            data: {
                criteria: [{ fieldName: 'country', operator: '=', value: country }],
            },
        });
        if (res && res.data) {
            return res.data;
        }
        throw new Error('No cities found');
    } catch (error) {
        console.error('Error fetching cities:', error);
        throw error;
    }
}
