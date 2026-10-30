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

const CUSTOMER_FIELDS = [
    'id',
    'simpleFullName',
    'firstName',
    'name',
    'nic',
    'fixedPhone',
    'mobilePhone',
    'mainAddress',
    'emailAddress',
    'dateOfBirth',
    'placeOfBirth',
    'loyaltyCard',
    'bankDetailsList',
    'isDoNotCall',
    'isDoNotEmail',
    'picture',
    'saleOrder',
    'saleOrderComments',
    'saleOrderInformation',
    'salePartnerPriceList',
    'socialMediaProfile',
];

async function fetchCustomerDetails(customerId) {
    try {
        const customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', customerId, { fields: CUSTOMER_FIELDS });
        if (!customerRes || !customerRes.data || !customerRes.data[0]) {
            throw new Error('Customer not found');
        }
        const customer = customerRes.data[0];

        // Fetch related data
        const mainAddressId = customer.mainAddress?.id;
        const loyaltyCardId = customer.loyaltyCard?.id;
        const emailAddressId = customer.emailAddress?.id;

        let mainAddress = null;
        if (mainAddressId) {
            const addressRes = await Service.search('com.axelor.apps.base.db.Address', {
                fields: ['id', 'fullName', 'addressL7Country', 'city', 'addressL4'],
                data: { criteria: [{ fieldName: 'id', operator: 'in', value: [mainAddressId] }] },
            });
            mainAddress = addressRes.data[0] || null;
        }

        let loyaltyCard = null;
        if (loyaltyCardId) {
            const loyaltyCardRes = await Service.search('com.axelor.pos.db.LoyaltyCard', {
                fields: ['id', 'cardNumber', 'creationDate', 'expirationDate', 'points'],
                data: { criteria: [{ fieldName: 'id', operator: 'in', value: [loyaltyCardId] }] },
            });
            loyaltyCard = loyaltyCardRes.data[0] || null;
        }

        let emailAddress = null;
        if (emailAddressId) {
            const emailAddressRes = await Service.search('com.axelor.message.db.EmailAddress', {
                fields: ['id', 'address'],
                data: { criteria: [{ fieldName: 'id', operator: 'in', value: [emailAddressId] }] },
            });
            emailAddress = emailAddressRes.data[0] || null;
        }

        // Merge related data into the customer object
        customer.mainAddress = mainAddress;
        customer.loyaltyCard = loyaltyCard;
        customer.emailAddress = emailAddress;

        return customer;
    } catch (error) {
        console.error('Error fetching customer details:', error);
        throw error;
    }
}

export async function fetchCustomers() {
    try {
        let res = await Service.search('com.axelor.apps.base.db.Partner', {
            fields: ['id'], // Only fetch the IDs initially
            data: {
                criteria: [{ fieldName: 'isCustomer', operator: '=', value: 'true' }],
            },
        });
        if (res && res.data) {
            const customerIds = res.data.map(customer => customer.id);

            // Fetch detailed information for each customer using fetchCustomerDetails
            const customerDetailsPromises = customerIds.map(id => fetchCustomerDetails(id));
            const customers = await Promise.all(customerDetailsPromises);

            return customers;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching customers:', error);
        throw error;
    }
}
function createMainAddress(data) {
    return {
        addressL7Country: data.addressL7Country || null,
        city: data.city || null,
        zip: data.city ? data.city.zip : null,
        addressL4: data.addressL4 ?? '',
        addressL6: data.city ? data.city.zip + " " + data.city.name : null,
        fullName: `${data.addressL4 || ''} ${data.city ? data.city.zip + " " + data.city.name : ''}`,
    };
}

export async function addCustomer(data) {
    try {
        const mainAddress = createMainAddress(data);
        let res = await Service.add('com.axelor.apps.base.db.Partner', {
            ...data,
            mainAddress: data.addressL7Country? mainAddress : null,
            isCustomer: true,
        });

        if (res && res.data && res.data[0] && res.data[0].id) {
            const customerId = res.data[0].id;
            return await fetchCustomerDetails(customerId);
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error adding customer:', error);
        throw error;
    }
}
async function updateEmailAddress(emailData) {
    try {
        const emailRes = await Service.add('com.axelor.message.db.EmailAddress', {...emailData});
        return emailRes.data[0];
    } catch (error) {
        console.error('Error updating email address:', error);
        throw error;
    }
}
async function updateMainAddress(addressData) {
    try {
        console.log('addressData: ', addressData)
        const mainAddressData = createMainAddress(addressData);
        if (addressData.id) {
            mainAddressData.id = addressData.id;
        }
        console.log("mainAddressData: ", mainAddressData)
        const addressRes = await Service.add('com.axelor.apps.base.db.Address', mainAddressData);
        console.log("main Address res: ", addressRes)
        return addressRes.data[0];
    } catch (error) {
        console.error('Error updating address:', error);
        throw error;
    }
}
async function updateLoyaltyCard(loyaltyCardData) {
    try {
        const loyaltyCardRes = await Service.add('com.axelor.pos.db.LoyaltyCard', loyaltyCardData);
        return loyaltyCardRes.data[0];
    } catch (error) {
        console.error('Error updating loyalty card:', error);
        throw error;
    }
}
export async function updateCustomer(data) {
    try {
        let {mainAddress, emailAddress,loyaltyCard,...remainingdata} = data
        console.log("remainingdata: ", remainingdata)
        console.log("mainAddress: ", mainAddress)
        console.log("emailAddress: ", emailAddress)
        console.log("loyaltyCard: ", loyaltyCard)
        let customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', data.id);
        let existingCustomer = customerRes.data[0];
        if (!existingCustomer) {
            throw new Error('Customer not found');
        }
        if (mainAddress) {
            console.log("mainAddress in api: ",mainAddress);
            mainAddress = await updateMainAddress({...mainAddress});
        }
        if (emailAddress) {
            console.log("emailAddress in api: ", {emailAddress});
            emailAddress = await updateEmailAddress({...emailAddress});
        }
        if (loyaltyCard) {
            console.log("loyaltyCard in api: ", loyaltyCard);
            loyaltyCard = await updateLoyaltyCard({...loyaltyCard});
        }

        const { mainAddress: _, ...existingCustomerData } = existingCustomer;
        const updateData =  {
            ...existingCustomerData,
            ...remainingdata,
            mainAddress: {id: mainAddress.id, fullName: mainAddress.fullName} || existingCustomer.mainAddress,
        }
        console.log('Data before updating customer:', updateData);
        console.log('existing customer: ', existingCustomer);
        let res = await Service.add('com.axelor.apps.base.db.Partner', updateData);
        console.log('Response after updating customer:', res);
        if (res && res.data && res.data[0] && res.data[0].id) {
            const customerId = res.data[0].id;
            return await fetchCustomerDetails(customerId);
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error updating customer:', error);
        throw error;
    }
}

export async function deleteCustomer(id) {
    try {
        let res = await Service.delete('com.axelor.apps.base.db.Partner', id);
        console.log('Response:', res);
        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting customer:', error);
        throw error;
    }
}

export async function deleteCustomerWithChildren(id) {
    try {
        // Fetch the customer data
        let customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', id);
        let customer = customerRes.data[0];

        if (!customer) {
            throw new Error('Customer not found');
        }

        console.log("Customer fetched:", customer);

        let addressId = customer.mainAddress ? customer.mainAddress.id : null;
        let emailId = customer.emailAddress ? customer.emailAddress.id : null;

        // Attempt to delete address if it exists
        if (addressId) {
            try {
                await Service.delete('com.axelor.apps.base.db.Address', addressId);
            } catch (error) {
                throw new Error(`Failed to delete address with ID ${addressId}: ${error.message}`);
            }
        }

        // Attempt to delete email address if it exists
        if (emailId) {
            try {
                await Service.delete('com.axelor.message.db.EmailAddress', emailId);
            } catch (error) {
                throw new Error(`Failed to delete email address with ID ${emailId}: ${error.message}`);
            }
        }

        // Delete customer/partner
        const res = await Service.delete('com.axelor.apps.base.db.Partner', id);

        return res;
        throw new Error('No data found');
    } catch (error) {
        console.error('Error deleting customer:', error);
        throw error;
    }
}

