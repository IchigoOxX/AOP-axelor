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

const PAYMENT_FIELDS = [

];


export async function fetchPaymentModes() {
    try {
        let res = await Service.search('com.axelor.apps.account.db.PaymentMode',{
            data: {
                criteria: [{ fieldName: 'inOutSelect', operator: '=', value: 1 }],
            }
        });
        console.log('Response:', res);
        if (res && res.data) {
            return res.data;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching payments:', error);
        throw error;
    }
}
export async function fetchPayments() {
    try {
        let res = await Service.search('com.axelor.apps.account.db.PaymentVoucher');
        console.log('Response:', res);
        if (res && res.status === 0) {
            return res.data;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching payments:', error);
        throw error;
    }
}

export async function addNewPayment(data) {
    try {
        console.log('data: ',data)
        data.company = data.user.activeCompany;
        const company = await Service.fetchId('com.axelor.apps.base.db.Company',data.company.id);
        console.log('company: ', company.data[0])
        data.currency = company.data[0].currency;
        data.operationTypeSelect = 3;
        data.paymentDate = new Date();
        data.statusSelect = 2;
        console.log('data sent: ', data)
        let res = await Service.add('com.axelor.apps.account.db.PaymentVoucher', data);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error adding payment:', error);
        throw error;
    }
}
export async function updatePayment(data) {
    try {
        let res = await Service.add('com.axelor.apps.account.db.PaymentVoucher', data);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error updating payment:', error);
        throw error;
    }
}

export async function deletePayment(id) {
    try {
        let res = await Service.delete('com.axelor.apps.account.db.PaymentVoucher', id);
        console.log('Response:', res);
        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting payment:', error);
        throw error;
    }
}