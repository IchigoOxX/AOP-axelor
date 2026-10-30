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

const SHIFT_FIELDS = [
    'id',
    'shiftNumber',
    'cashier',
    'shiftDate',
    'openingHour',
    'closingHour',
    'status',
    'startingCash',
    'totalSales',
    'totalRefunds',
    'totalDiscounts',
    'cashBalance',
    'cashIn',
    'cashOut',
    'totalTransactions',
    'numberOfSales',
    'itemsSold',
    'refrence',
    'notes'
];

export async function fetchShifts() {
    try {
        let res = await Service.search('com.axelor.pos.db.Shift', {
            fields: SHIFT_FIELDS,
            /*data: {
                criteria: USERS SHIFTS ONLY/ADMIN ALL SHIFTS,
            },*/
        });
        console.log('Response:', res);
        if (res && res.status === 0) {
            return res.data;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching shifts:', error);
        throw error;
    }
}

export async function addShift(data) {
    try {
        let res = await Service.add('com.axelor.pos.db.Shift', data);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error adding shift:', error);
        throw error;
    }
}
export async function updateShift(data) {
    try {
        let res = await Service.add('com.axelor.pos.db.Shift', data);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error updating shift:', error);
        throw error;
    }
}
export async function updateShiftAfterSale(data) {
    try {
        console.log('data: ', data)
        let res = await Service.post('/ws/shifts/updateAfterSale', {data});
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error updating shift:', error);
        throw error;
    }
}

// export async function updateShiftAfterSale(shiftId,SaleOrder) {
//     try {
//         // const res = await Service.fetchId('')
//
//
//         let res = await Service.add('com.axelor.pos.db.Shift', data);
//         console.log('Response:', res);
//         if (res && res.data && res.data[0]) {
//             return res.data[0];
//         }
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error updating shift:', error);
//         throw error;
//     }
// }

export async function deleteShift(id) {
    try {
        let res = await Service.delete('com.axelor.pos.db.Shift', id);
        console.log('Response:', res);
        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting shift:', error);
        throw error;
    }
}