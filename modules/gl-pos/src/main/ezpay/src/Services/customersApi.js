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
    'loyaltyCard.id',
    'bankDetailsList',
    'isActive',
    'isDoNotCall',
    'isDoNotEmail',
    'picture',
    'salesOrders',
    'saleOrderComments',
    'saleOrderInformation',
    'salePartnerPriceList',
    'socialMediaProfile',
];

export async function fetchCustomers() {
    try {
        let res = await Service.search('com.axelor.apps.base.db.Partner', {
            fields: CUSTOMER_FIELDS,
            data: {
                criteria: [{ fieldName: 'isCustomer', operator: '=', value: 'true' }],
            },
        });
        console.log('Response:', res);
        if (res && res.data) {
            const customers = res.data;

            // Extract mainAddress IDs, loyaltyCard IDs, and emailAddress IDs from customers
            const mainAddressIds = customers
                .map(customer => customer.mainAddress?.id)
                .filter(id => id); // Remove undefined or null IDs

            const loyaltyCardIds = customers
                .map(customer => customer['loyaltyCard.id'])
                .filter(id => id); // Remove undefined or null IDs

            const emailAddressIds = customers
                .map(customer => customer.emailAddress?.id)
                .filter(id => id); // Remove undefined or null IDs

            // Fetch mainAddress details
            let addresses = [];
            if (mainAddressIds.length > 0) {
                const addressRes = await Service.search('com.axelor.apps.base.db.Address', {
                    fields: ['id','fullName', 'addressL7Country', 'city', 'addressL4'],
                    data: {
                        criteria: [{ fieldName: 'id', operator: 'in', value: mainAddressIds }],
                    },
                });

                if (addressRes && addressRes.data) {
                    addresses = addressRes.data;
                }
            }

            // Fetch loyaltyCard details
            let loyaltyCards = [];
            if (loyaltyCardIds.length > 0) {
                const loyaltyCardRes = await Service.search('com.axelor.pos.db.LoyaltyCard', {
                    fields: ['id', 'cardNumber', 'creationDate', 'expirationDate', 'points'],
                    data: {
                        criteria: [{ fieldName: 'id', operator: 'in', value: loyaltyCardIds }],
                    },
                });

                if (loyaltyCardRes && loyaltyCardRes.data) {
                    loyaltyCards = loyaltyCardRes.data;
                }
            }

            // Fetch emailAddress details
            let emailAddresses = [];
            if (emailAddressIds.length > 0) {
                const emailAddressRes = await Service.search('com.axelor.message.db.EmailAddress', {
                    fields: ['id', 'address'],
                    data: {
                        criteria: [{ fieldName: 'id', operator: 'in', value: emailAddressIds }],
                    },
                });

                if (emailAddressRes && emailAddressRes.data) {
                    emailAddresses = emailAddressRes.data;
                }
            }

            // Map the fetched addresses, loyalty cards, and email addresses by their IDs
            const addressMap = new Map(addresses.map(address => [address.id, address]));
            const loyaltyCardMap = new Map(loyaltyCards.map(card => [card.id, card]));
            const emailAddressMap = new Map(emailAddresses.map(email => [email.id, email]));

            // Merge address, loyalty card, and email address data back into customers
            customers.forEach(customer => {
                if (customer.mainAddress && addressMap.has(customer.mainAddress.id)) {
                    customer.mainAddress = addressMap.get(customer.mainAddress.id);
                }
                if (customer['loyaltyCard.id'] && loyaltyCardMap.has(customer['loyaltyCard.id'])) {
                    customer.loyaltyCard = loyaltyCardMap.get(customer['loyaltyCard.id']);
                }
                if (customer.emailAddress && emailAddressMap.has(customer.emailAddress.id)) {
                    customer.emailAddress = emailAddressMap.get(customer.emailAddress.id);
                }
            });

            return customers;
        }
        throw new Error('No data found');
    } catch (error) {
        console.error('Error fetching customers:', error);
        throw error;
    }
}

// function createMainAddress(data) {
//     console.log("address data: ", data)
//     return {
//         addressL7Country: data.addressL7Country || null,
//         city: data.city || null,
//         zip: data.city ? data.city.zip : null,
//         addressL4: data.addressL4 ?? '',
//         addressL6: data.city ? data.city.zip + " " + data.city.name : null,
//         fullName: `${data.addressL4 || ''} ${data.city ? data.city.zip + " " + data.city.name : ''} ${data.addressL7Country ? data.addressL7Country.name : ''}`.trim()
//     };
// }

function createMainAddress(data) {
    console.log("address data: ", data);

    const cityName = data.city && data.city.name ? data.city.name : '';
    const cityZip = data.city && data.city.zip ? data.city.zip : '';
    const countryName = data.addressL7Country && data.addressL7Country.name ? data.addressL7Country.name : '';
    const addressL4 = data.addressL4 || '';

    return {
        addressL7Country: data.addressL7Country || null,
        city: data.city || null,
        zip: cityZip || null,
        addressL4: addressL4,
        addressL6: cityZip && cityName ? `${cityZip} ${cityName}` : '',
        fullName: `${addressL4} ${cityZip} ${cityName} ${countryName}`.trim(),
    };
}
export async function addCustomer(data) {
    try {
        let {
            mainAddress,
            emailAddress,
            ...customerData
        } = data;
        mainAddress = createMainAddress(mainAddress);

        let res = await Service.add('com.axelor.apps.base.db.Partner', {
            ...customerData,
            emailAddress: Object.keys(emailAddress).length ? emailAddress : null,
            mainAddress: mainAddress.fullName ? mainAddress : null,
            isCustomer: true,
        });
        if (res && res.data && res.data[0]) {
            return res.data[0];
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
        const mainAddressData = createMainAddress(addressData);
        if (addressData.id) {
            addressData = await Service.fetchId('com.axelor.apps.base.db.Address',addressData.id);
            console.log('address 1: ',addressData);
             addressData = {
                ...addressData,
                ...mainAddressData
            }
            console.log('address 2: ',addressData);
        }
        console.log('address 3: ',addressData);
        const addressRes = await Service.add('com.axelor.apps.base.db.Address', addressData);
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
// export async function updateCustomer(data) {
//     try {
//         let customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', data.id);
//         let existingCustomer = customerRes.data[0];
//
//         if (!existingCustomer) {
//             throw new Error('Customer not found');
//         }
//         let res = await Service.add('com.axelor.apps.base.db.Partner', {
//             ...existingCustomer,
//             ...data,
//         });
//
//         console.log('Response:', res);
//         if (res && res.data && res.data[0]) {
//             return res.data[0];
//         }
//         throw new Error('No data found');
//     } catch (error) {
//         console.error('Error updating customer:', error);
//         throw error;
//     }
// }
export async function updateCustomer(data) {
    try {
        let { mainAddress, emailAddress, loyaltyCard, ...remainingData } = data;
        console.log("remainingData: ", remainingData);
        console.log("mainAddress: ", mainAddress);
        console.log("emailAddress: ", emailAddress);
        console.log("loyaltyCard: ", loyaltyCard);

        let customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', data.id);
        let existingCustomer = customerRes.data[0];

        if (!existingCustomer) {
            throw new Error('Customer not found');
        }

        // Update mainAddress if it exists and is valid
        if (mainAddress) {
            console.log("Updating mainAddress: ", mainAddress);
            mainAddress = await updateMainAddress(mainAddress);
        } else {
            mainAddress = existingCustomer.mainAddress;
        }
        if (!existingCustomer.mainAddress) {
            remainingData = {...remainingData, mainAddress: {id: mainAddress.id}}
        }

        // Update emailAddress if it exists and is valid
        if (emailAddress) {
            console.log("Updating emailAddress: ", emailAddress);
            emailAddress = await updateEmailAddress(emailAddress);
        } else {
            emailAddress = existingCustomer.emailAddress;
        }
        if (!existingCustomer.emailAddress) {
            remainingData = {...remainingData, emailAddress}
        }

        // Update loyaltyCard if it exists and is valid
        if (loyaltyCard) {
            console.log("Updating loyaltyCard: ", loyaltyCard);
            loyaltyCard = await updateLoyaltyCard(loyaltyCard);
        } else {
            loyaltyCard = existingCustomer.loyaltyCard;
        }

        customerRes = await Service.fetchId('com.axelor.apps.base.db.Partner', data.id);
        existingCustomer = customerRes.data[0]
        const updateData = {
            ...existingCustomer,
            ...remainingData
        };

        console.log('Data before updating customer:', updateData);
        console.log('Existing customer: ', existingCustomer);

        let res = await Service.add('com.axelor.apps.base.db.Partner', updateData);

        if (res && res.data && res.data[0]) {
            return res.data[0];
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
