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

const SALE_ORDER_LINE_FIELDS = [

];

export async function fetchSaleOrderLines() {
    try {
        let res = await Service.search('com.axelor.apps.sale.db.SaleOrderLine', {
            fields: SALE_ORDER_LINE_FIELDS,
        });
        console.log('Response:', res);
        if (res && res.data) {
            return res.data;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching sale order lines:', error);
        throw error;
    }
}

export async function addSaleOrderLine(data) {
    try {
        const  { saleOrderLineList, ...saleOrder } = data
        let res = await Service.add('com.axelor.apps.sale.db.SaleOrderLine', saleOrder);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error adding sale order line:', error);
        throw error;
    }
}
export async function updateSaleOrderLine(data) {
    try {
        let res = await Service.add('com.axelor.apps.sale.db.SaleOrderLine', data);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            return res.data[0];
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error updating sale order line:', error);
        throw error;
    }
}

export async function deleteSaleOrderLine(id) {
    try {
        let res = await Service.delete('com.axelor.apps.sale.db.SaleOrderLine', id);
        console.log('Response:', res);
        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting sale order line:', error);
        throw error;
    }
}