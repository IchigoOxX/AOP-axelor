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

const USER_FIELDS = [
    'id',
    'fullName',
    'name',
    'code',
    'email',
    'image',
    'store',
    'shifts',
    'activeCompany'
];

export async function fetchUsers() {
    try {
        let res = await Service.search('com.axelor.auth.db.User');
        if (res && res.data) {
            return res.data;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching customers:', error);
        throw error;
    }
}

// export async function fetchCurrentUser() {
//     try {
//         let res = await Service.get('/ws/app/info');
//         if (res && res['user.id']) {
//             return res['user.id'];
//         }
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error fetching current user:', error);
//         throw error;
//     }
// }

export async function fetchCurrentUser() {
    try {
        // Fetch the current user info to get the user ID
        let res = await Service.get('/ws/app/info');
        if (res && res['user.id']) {
            const userId = res['user.id'];

            const userDetails = await Service.fetchId('com.axelor.auth.db.User', userId, {fields: USER_FIELDS} );

            if (userDetails && userDetails.data) {
                const activeCompanyId = userDetails.data[0].activeCompany.id;
                const activeCompany = await Service.fetchId('com.axelor.apps.base.db.Company', activeCompanyId);
                return userDetails.data[0];
            }
            throw new Error('No user details found');
        }
        throw new Error('No user ID found');
    } catch (error) {
        console.error('Error fetching current user:', error);
        throw error;
    }
}
