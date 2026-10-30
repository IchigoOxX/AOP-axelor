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

const PRODUCT_FIELDS = [
  'id',
  'name',
  'code',
  'createdOn',
  'barCode',
  'productCategory',
  'productFamily',
  'brand',
  'description',
  'picture',
  'has_warranty',
  'diameter',
  'length',
  'height',
  'width',
  'purchasePrice',
  'salePrice',
  'costPrice',
  'unit',
  'purchaseCurrency',
  'purchaseUnit',
  'saleCurrency',
  'salesUnit',
  'discountAvailable',
  'discountAmount',
  'discountPercentage',
  'offerAvailable',
  'offerDiscountPercentage',
  'offersStartsAtQty',
  'stockManaged',
  'availableInStore',
  'placementInStore',
  'reorderLevel',
  'reorderQty',
  'salesRank',
  'totalSold',
];

// const CATEGORY_FIELDS = ['name'];
const CATEGORY_FIELDS = ['id', 'name', 'code'];

function getEarlierDate(date1, date2) {
  if (!date1) return date2;
  if (!date2) return date1;
  return new Date(date1) < new Date(date2) ? date1 : date2;
}
export async function fetchProductBasicInfo(productId) {
  try {
    const productRes = await Service.fetchId('com.axelor.apps.base.db.Product', productId, {
      fields: ['id', 'name', 'salePrice', 'picture'],
    });
    const product = productRes.data[0] || null;
    if (product) {
      product.imageUrl = Service.getImageUrl('com.axelor.meta.db.MetaFile', product.picture);
    }
    return product;
  } catch (error) {
    console.error('Error fetching product basic info:', error);
    return null;
  }
}
export async function fetchProducts() {
  try {
    let productRes = await Service.search('com.axelor.apps.base.db.Product', {
      fields: PRODUCT_FIELDS,
    });

    if (productRes && productRes.data) {
      const products = productRes.data;

      let stockRes = await Service.search('com.axelor.apps.stock.db.StockMoveLine', {
        fields: [
          'id',
          'stockMove.realDate',
          'stockMove.estimatedDate',
          'product.id',
          'realQty'
        ],
      });

      let saleRes = await Service.search('com.axelor.apps.sale.db.SaleOrderLine', {
        fields: [
          'product.id',
          'qty',
          'saleOrder.id', // Include the SaleOrder ID
        ],
      });

      let saleOrderRes = await Service.search('com.axelor.apps.sale.db.SaleOrder', {
        fields: [
          'id',
          'statusSelect'
        ],
      });

      // Create a map to store total stock and earliest arrival date for each product
      let stockMap = new Map();
      if (stockRes && stockRes.data) {
        stockRes.data.forEach(stockMoveLine => {
          const productId = stockMoveLine['product.id'];
          const quantity = stockMoveLine['realQty'] || 0;
          const realDate = stockMoveLine['stockMove.realDate'];
          const estimatedDate = stockMoveLine['stockMove.estimatedDate'];

          if (stockMap.has(productId)) {
            const productData = stockMap.get(productId);
            productData.stock += quantity;
            productData.arrivalDate = getEarlierDate(productData.arrivalDate, realDate || estimatedDate);
            stockMap.set(productId, productData);
          } else {
            stockMap.set(productId, {
              stock: quantity,
              arrivalDate: realDate || estimatedDate
            });
          }
        });

        // Create a map for SaleOrder statuses
        let saleOrderStatusMap = new Map();
        if (saleOrderRes && saleOrderRes.data) {
          saleOrderRes.data.forEach(saleOrder => {
            saleOrderStatusMap.set(saleOrder.id, saleOrder.statusSelect);
          });
        }

        // Create a map to store total sold items for completed orders
        let salesMap = new Map();
        if (saleRes && saleRes.data) {
          saleRes.data.forEach(saleOrderLine => {
            const saleOrderId = saleOrderLine['saleOrder.id'];
            const productId = saleOrderLine['product.id'];
            const qtySold = parseInt(saleOrderLine['qty']) || 0;

            // Check if the sale order is completed
            if (saleOrderStatusMap.get(saleOrderId) === 4) { // Adjust 'Completed' to the actual status value
              if (salesMap.has(productId)) {
                const productData = salesMap.get(productId);
                productData.soldItems += qtySold;
                salesMap.set(productId, productData);
              } else {
                salesMap.set(productId, {
                  soldItems: qtySold
                });
              }
            }
          });
        }

        const appPosParam = await Service.get('/ws/rest/com.axelor.studio.db.AppPos');
        const warehouseLocation = appPosParam?.data[0]?.warehouseLocation;
        for (const product of products) {
          const stockLocationLineRes = await Service.search('com.axelor.apps.stock.db.StockLocationLine', {
            data: {
              operator: 'and',
              criteria: [
                {
                  fieldName: 'product.id',
                  operator: '=',
                  value: `${product.id}`
                },
                {
                  fieldName: 'stockLocation.id',
                  operator: '=',
                  value: `${warehouseLocation.id}`
                }
              ],
            },
          });
          product.stock = stockLocationLineRes.data ? stockLocationLineRes.data[0].currentQty : 0 ;
        }



        // Add stock, arrival date, and total sold items to each product
        products.forEach(product => {
          const stockData = stockMap.get(product.id);
          const salesData = salesMap.get(product.id);
          // product.stock = stockData ? stockData.stock : 0;
          product.arrivalDate = stockData ? stockData.arrivalDate : null;
          product.imageUrl = Service.getImageUrl('com.axelor.meta.db.MetaFile', product.picture);
          product.totalSold = salesData ? salesData.soldItems : 0;
        });
      }
      return products;
    }
  } catch (error) {
    console.error('Error fetching products or stock data:', error);
    return [];
  }
}

// export async function fetchProducts() {
//   let res = await Service.search('com.axelor.apps.base.db.Product', {
//     fields: PRODUCT_FIELDS,
//   });
//   // console.log('Response:', res);
//   if (res && res.data) {
//     return res.data;
//   }
// }

export async function fetchCategories() {
  let res = await Service.search('com.axelor.apps.base.db.ProductCategory', {
    fields: CATEGORY_FIELDS,
  });
  console.log('Response:', res);
  if (res && res.data) {
    return res.data;
  }
}


