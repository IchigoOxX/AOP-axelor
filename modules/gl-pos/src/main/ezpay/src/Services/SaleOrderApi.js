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
import {fetchProductBasicInfo} from "./productsApi";

const SALE_ORDER_FIELDS = [
    'saleOrderSeq',
    'orderNumber',
    'fullName',
    'clientPartner',
    'totalCostPrice',
    'accountedRevenue',
    'saleOrderLineList',
    'amountInvoiced',
    'salespersonUser',
    'externalRefrence',
    'creationDate',
    'orderDate',
    'statusSelect',
    'currency',
    'orderBeingEdited',
    'createdOn',
    'UpdatedOn'
];

async function fetchClientPartner(partnerId) {
    try {
        const partnerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', partnerId, {
            fields: ['id', 'fullName', 'simpleFullName', 'emailAddress', 'mobilePhone'],
        });
        return partnerRes.data[0] || null;
    } catch (error) {
        console.error('Error fetching client partner:', error);
        return null;
    }
}

async function fetchSaleOrderLineList(saleOrderLineIds) {
    try {
        const saleOrderLineRes = await Service.search('com.axelor.apps.sale.db.SaleOrderLine', {
            fields: ['id', 'productName', 'qty', 'product'],
            data: { criteria: [{ fieldName: 'id', operator: 'in', value: saleOrderLineIds }] },
        });
        const saleOrderLines = saleOrderLineRes.data || [];

        // Fetch product details for each sale order line
        const saleOrderLinesWithProducts = await Promise.all(
            saleOrderLines.map(async (line) => {
                const product = await fetchProductBasicInfo(line.product.id);
                return { ...line, product };
            })
        );

        return saleOrderLinesWithProducts;
    } catch (error) {
        console.error('Error fetching sale order line list:', error);
        return [];
    }
}

export async function fetchSaleOrders() {
    try {
        let res = await Service.search('com.axelor.apps.sale.db.SaleOrder', {
            fields: SALE_ORDER_FIELDS,
            sortBy: ['-id'],
        });
        console.log('Response:', res);
        if (res && res.data) {
            const saleOrders = res.data;

            // Fetch related data for each sale order
            const saleOrdersWithDetails = await Promise.all(
                saleOrders.map(async (saleOrder) => {
                    const clientPartnerId = saleOrder.clientPartner?.id;
                    const saleOrderLineIds = saleOrder.saleOrderLineList?.map(line => line.id) || [];

                    const clientPartner = clientPartnerId ? await fetchClientPartner(clientPartnerId) : null;
                    const saleOrderLineList = saleOrderLineIds.length > 0 ? await fetchSaleOrderLineList(saleOrderLineIds) : [];

                    return {
                        ...saleOrder,
                        clientPartner,
                        saleOrderLineList,
                    };
                })
            );
            return saleOrdersWithDetails;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching sale orders:', error);
        throw error;
    }
}

export async function fetchSaleOrderById(saleOrderId) {
    try {
        let res = await Service.fetchId('com.axelor.apps.sale.db.SaleOrder', saleOrderId );
        console.log('Response:', res);
        if (res && res.data[0]) {
            return res.data[0];
        } else {
            throw new Error('No data found');
        }
    } catch (error) {
        console.error('Error fetching sale orders:', error);
        throw error;
    }
}

export async function getLastOrderNumber() {
    try {
        const res = await Service.search('com.axelor.apps.sale.db.SaleOrder', {
            fields: ['orderNumber'],
            sortBy: ['-id'],
            limit: 1,
        });
        if (res && res.data && res.data.length > 0) {
            console.log('last order: ', res.data[0].orderNumber);
            return res.data[0].orderNumber;
        } else {
            // Handle the case where no sale orders are found
            throw new Error('No sale orders found.');
        }
    } catch (error) {
        console.error('Error fetching next sale order number:', error);
        throw error;
    }
}

// export async function fetchSaleOrders() {
//     try {
//         let res = await Service.search('com.axelor.apps.sale.db.SaleOrder', {
//             fields: SALE_ORDER_FIELDS,
//         });
//         console.log('Response:', res);
//         if (res && res.data) {
//             return res.data;
//         }
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error fetching sale orders:', error);
//         throw error;
//     }
// }

export async function addSaleOrderLine(data) {
    try {
        let res = await Service.add('com.axelor.apps.sale.db.SaleOrderLine', data);
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

export async function addSaleOrder(data) {
    try {
        const { saleOrderLineList = [], ...saleOrder } = data;
        let res = await Service.add('com.axelor.apps.sale.db.SaleOrder', saleOrder);
        console.log('Response:', res);
        if (res && res.data && res.data[0]) {
            let saleOrder = res.data[0];

            const addedLines = await Promise.all(
                saleOrderLineList.map(async (saleOrderLine) => {
                    const lineData = { ...saleOrderLine, saleOrder: { id: saleOrder.id }, unit: saleOrderLine.product.unit };
                    return await addSaleOrderLine(lineData);
                })
            );
            saleOrder = {...saleOrder, saleOrderLineList: addedLines}
            console.log('saleOrder: ', saleOrder)
            const saleOrderLineIds = saleOrder.saleOrderLineList?.map(line => line.id) || [];
            const saleOrderLineListWithDetails = await fetchSaleOrderLineList(saleOrderLineIds);

            return { ...saleOrder, saleOrderLineList: saleOrderLineListWithDetails };
            // return { ...res.data[0], saleOrderLineList: addedLines };
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error adding sale order:', error);
        throw error;
    }
}
export async function updateSaleOrderFields(data) {
    const { saleOrderId, ...fieldsToUpdate } = data;
    try {
        // Fetch the existing sale order by ID
        const res = await Service.fetchId('com.axelor.apps.sale.db.SaleOrder', saleOrderId, {
            fields: SALE_ORDER_FIELDS,
        });

        if (res && res.data && res.data[0]) {
            console.log('saleorder update res(fetched): ', res)
            const saleOrder = res.data[0];

            // Update the fetched sale order with new values
            Object.keys(fieldsToUpdate).forEach(field => {
                saleOrder[field] = fieldsToUpdate[field];
            });
            console.log('saleorder update res(modified): ', saleOrder)
            // Save the updated sale order
            const updateRes = await Service.add('com.axelor.apps.sale.db.SaleOrder', saleOrder);
            console.log('Updated Response:', updateRes);
            if (updateRes && updateRes.data && updateRes.data[0]) {
                console.log('saleorder update res(new): ', updateRes)
                return updateRes.data[0];
            }
            throw new Error('No data found');
        }
        throw new Error('Sale order not found');
    } catch (error) {
        console.error('Error updating sale order:', error);
        throw error;
    }
}

// export async function updateSaleOrder(data) {
//     try {
//         let res = await Service.add('com.axelor.apps.sale.db.SaleOrder', data);
//         console.log('Response:', res);
//         if (res && res.data && res.data[0]) {
//             return res.data[0];
//         }
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error updating sale order:', error);
//         throw error;
//     }
// }

export async function deleteSaleOrder(id) {
    try {
        let res = await Service.delete('com.axelor.apps.sale.db.SaleOrder', id);
        console.log('Response:', res);
        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting sale order:', error);
        throw error;
    }
}

// export async function completeSaleOrder(saleOrderId) {
//     const res = await Service.fetchId('com.axelor.apps.sale.db.SaleOrder', saleOrderId);
//     const saleOrder = res.data[0];
//     console.log("saleOrder in api: ", saleOrder)
//     const model = 'com.axelor.apps.sale.db.SaleOrder';
//     const mode = 'com.gl.apps.pos.web.PosController:confirmSaleOrder';
//     const data = {
//         model,
//         domain: null,
//         context: {
//             id: saleOrder.id,
//         }
//     };
//     console.log("data payload: ", data)
//     try {
//         let res = await Service.post(`/ws/action/${method}`, data);
//         console.log('Response:', res);
//         return res;
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error completing sale order:', error);
//         throw error;
//     }
// }
export async function comfirmSaleOrder(saleOrderId) {
    const res = await Service.fetchId('com.axelor.apps.sale.db.SaleOrder', saleOrderId);
    const saleOrder = res.data[0];
    console.log("saleOrder in api: ", saleOrder)
    try {
        const data = {
            data: saleOrder,
        };
        let res = await Service.post(`/ws/saleOrder/confirmSaleOrder`, data);
        console.log('Response:', res);
        if (res && res.data) {
            const confirmedSaleOrder = res.data;
            // Fetch the updated sale order with sale order lines
            const saleOrderLineIds = confirmedSaleOrder.saleOrderLineList?.map(line => line.id) || [];
            const saleOrderLineListWithDetails = await fetchSaleOrderLineList(saleOrderLineIds);
            const saleOrder = {...confirmedSaleOrder, saleOrderLineList: saleOrderLineListWithDetails}
            console.log("response after modif: ", saleOrder)
            return saleOrder;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error confirming sale order:', error);
        throw error;
    }
}
export async function completeSaleOrder(saleOrderId) {
    const res = await Service.fetchId('com.axelor.apps.sale.db.SaleOrder', saleOrderId);
    const saleOrder = res.data[0];
    console.log("saleOrder in api: ", saleOrder)
    try {
        const data = {
            data: saleOrder,
        };
        let res = await Service.post(`/ws/saleOrder/completeSaleOrder`, data);
        console.log('Response:', res);
        if (res && res.data) {
            const completedSaleOrder = res.data;

            // Fetch the updated sale order with sale order lines
            const saleOrderLineIds = completedSaleOrder.saleOrderLineList?.map(line => line.id) || [];
            const saleOrderLineListWithDetails = await fetchSaleOrderLineList(saleOrderLineIds);

            return { ...completedSaleOrder, saleOrderLineList: saleOrderLineListWithDetails };
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error completing sale order:', error);
        throw error;
    }
}