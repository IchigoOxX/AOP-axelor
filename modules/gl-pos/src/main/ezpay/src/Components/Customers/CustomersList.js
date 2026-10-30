import React, { useEffect, useState } from 'react';
import styled from 'styled-components';
import { useDispatch, useSelector } from 'react-redux';
import MoreHorizIcon from '@mui/icons-material/MoreHoriz';
import { Menu, MenuItem } from '@mui/material';
import PersonOutlineOutlinedIcon from '@mui/icons-material/PersonOutlineOutlined';
import ModeEditOutlineOutlinedIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import SwapVertOutlinedIcon from '@mui/icons-material/SwapVertOutlined';
import { useNavigate } from 'react-router-dom';
import { deleteCustomerThunk, fetchCustomersThunk } from '../Redux/customerSlice';
import customersList from "../CartPanel/CustomersList";
import DeleteConfirm from "../Common/DeleteConfirm";

const Container = styled.div`
  margin-bottom: 20px;
  overflow-x: auto;
`;
const CustomersTable = styled.table`
  margin: auto;
  background-color: #f1f7fb;
  border-radius: 10px;
  width: calc(100% - 40px);
  overflow: auto;
  text-align: left;
  border-collapse: collapse;
  & > th,
  tr {
    padding: 10px;
  }
`;
const TableRow = styled.tr`
  border-top: solid lightgrey 1px;
  &:hover {
    background-color: #e0f3ff;
    cursor: pointer;
  }
`;
const Th = styled.th`
  padding: 7px;
`;
const Td = styled.td`
  padding: 5px;
`;
const Input = styled.input`
  border: none;
  outline-style: none;
  background-color: transparent;
`;
const SortIcon = styled(SwapVertOutlinedIcon)`
  vertical-align: middle;
  margin: 5px;
  cursor: pointer;
`;
const MoreIcon = styled(MoreHorizIcon)`
  border-radius: 5px;
  cursor: pointer;
  &:hover {
    background-color: lightgrey;
  }
`;

const CustomersList = ({filters, setShowButton, selectedCustomers, setSelectedCustomers }) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const customers = useSelector(state => state.customers.customersList);
  const loading = useSelector(state => state.customers.loading);
  const error = useSelector(state => state.customers.error);
  useEffect(() => {
    dispatch(fetchCustomersThunk())
      .then(response => {
        console.log('Customers fetched:', response.payload);
      })
      .catch(error => {
        console.error('Error fetching customers:', error);
      });
  }, [dispatch]);

  const [menuState, setMenuState] = useState({
    anchorEl: null,
    customerId: null,
  });
  const [isAscending, setIsAscending] = useState(true);
  const [searchFields, setSearchFields] = useState({
    name: '',
    email: '',
    phone: '',
    address: '',
    loyaltyCard: '',
  });

  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleteCustomerId, setDeleteCustomerId] = useState(null);

  const handleSelectCustomer = customerId => {
    setSelectedCustomers(prevSelected => {
      const newSelected = prevSelected.includes(customerId)
        ? prevSelected.filter(id => id !== customerId)
        : [...prevSelected, customerId];

      setShowButton(newSelected.length > 0);
      return newSelected;
    });
  };
  const handleSelectAll = () => {
    if (selectedCustomers.length === customers.length) {
      setSelectedCustomers([]);
      setShowButton(false);
    } else {
      setSelectedCustomers(customers.map(customer => customer.id));
      setShowButton(true);
    }
  };
  const isAllSelected = customers.length > 0 && selectedCustomers.length === customers.length;

  const handleClick = (event, customerId) => {
    setMenuState({ anchorEl: event.currentTarget, customerId });
  };
  const handleClose = () => {
    setMenuState({ anchorEl: null, customerId: null });
  };
  const handleDetails = customer => {
    handleClose();
    navigate('/customers/customer-details', {
      state: { customer, mode: 'view' },
    });
  };
  const handleEdit = customer => {
    handleClose();
    navigate('/customers/customer-details', {
      state: { customer, mode: 'edit' },
    });
  };
  // const handleDelete = id => {
  //   console.log('customer id: ', id);
  //   dispatch(deleteCustomerThunk(id));
  //   handleClose();
  // };
  const handleDelete = (id) => {
    setDeleteCustomerId(id);
    setShowDeleteConfirm(true);
    handleClose();
  };

  const handleConfirmDelete = () => {
    if (deleteCustomerId) {
      dispatch(deleteCustomerThunk(deleteCustomerId));
      setShowDeleteConfirm(false);
    }
  };

  const handleCancelDelete = () => {
    console.log("close")
    setShowDeleteConfirm(false);
    setDeleteCustomerId(null);
  };
  const handleSort = () => {
    setIsAscending(!isAscending);
  };
  const sortedCustomers = [...customers].sort((a, b) => {
    if (a.simpleFullName?.toLowerCase() < b.simpleFullName?.toLowerCase()) return isAscending ? -1 : 1;
    if (a.simpleFullName?.toLowerCase() > b.simpleFullName?.toLowerCase()) return isAscending ? 1 : -1;
    return 0;
  });

  const handleSearchChange = field => event => {
    setSearchFields({ ...searchFields, [field]: event.target.value });
  };
  const applySearch = (customer) => {
    const { name, email, phone, address, loyaltyCard } = searchFields;
    const fullName = customer.simpleFullName?.toLowerCase() || '';
    const emailAddress = customer.emailAddress?.address?.toLowerCase() || '';
    const phoneNumber = (customer.mobilePhone || customer.fixedPhone)?.toLowerCase() || '';
    const mainAddress = customer.mainAddress?.fullName?.toLowerCase() || '';
    const cardNumber = customer.loyaltyCard?.cardNumber?.toLowerCase() || '';

    return (
        fullName.includes(name.toLowerCase()) &&
        emailAddress.includes(email.toLowerCase()) &&
        phoneNumber.includes(phone.toLowerCase()) &&
        mainAddress.includes(address.toLowerCase()) &&
        cardNumber.includes(loyaltyCard.toLowerCase())
    );
  };

  // Filter Function
  const applyFilters = (customer) => {
    return (
        (!filters.activeCustomers || customer.isActive) &&
        (!filters.inactiveCustomers || !customer.isActive) &&
        (!filters.mostPurchases || customer.salesOrders?.length > 0)
    );
  };

  // Apply Search and Filters
  const filteredCustomers = customers
      .filter(customer => applySearch(customer))
      .filter(customer => applyFilters(customer))
      .filter(customer => !filters.mostPurchases || (customer.salesOrders && customer.salesOrders.length > 0))
      .sort((a, b) => {
        if (filters.mostPurchases) {
          return b.salesOrders?.length - a.salesOrders?.length; // Sort by number of purchases if 'Most Purchases' is active
        } else {
          if (a.simpleFullName?.toLowerCase() < b.simpleFullName?.toLowerCase()) return isAscending ? -1 : 1;
          if (a.simpleFullName?.toLowerCase() > b.simpleFullName?.toLowerCase()) return isAscending ? 1 : -1;
          return 0;
        }
      })
      .slice(0, filters.mostPurchases ? 20 : undefined); // Take top 10 if 'Most Purchases' is active

  console.log('filtered customers: ', filteredCustomers);


  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <Container>
      <CustomersTable>
        <thead>
          <tr>
            <Th>
              <input type="checkbox" checked={isAllSelected} onChange={handleSelectAll} />
            </Th>
            <Th>
              Full Name
              <SortIcon onClick={handleSort} />
            </Th>
            <Th>Email</Th>
            <Th>Phone</Th>
            <Th>Address</Th>
            <Th>Loyalty Card Number</Th>
            <Th>Points</Th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <Td></Td>
            <Td>
              <Input
                type="text"
                value={searchFields.name}
                onChange={handleSearchChange('name')}
                placeholder="Search Name"
              />
            </Td>
            <Td>
              <Input
                type="text"
                value={searchFields.email}
                onChange={handleSearchChange('email')}
                placeholder="Search Email"
              />
            </Td>
            <Td>
              <Input
                type="text"
                value={searchFields.phone}
                onChange={handleSearchChange('phone')}
                placeholder="Search Phone"
              />
            </Td>
            <Td>
              <Input
                type="text"
                value={searchFields.address}
                onChange={handleSearchChange('address')}
                placeholder="Search Address"
              />
            </Td>
            <Td>
              <Input
                type="text"
                value={searchFields.loyaltyCard}
                onChange={handleSearchChange('loyaltyCard')}
                placeholder="Search Loyalty Card"
              />
            </Td>
          </tr>
          {filteredCustomers.map(customer => (
            <TableRow key={customer.id} onDoubleClick={() => handleDetails(customer)}>
              <Td>
                <input
                  type="checkbox"
                  checked={selectedCustomers.includes(customer.id)}
                  onChange={() => handleSelectCustomer(customer.id)}
                />
              </Td>
              <Td>{customer.simpleFullName}</Td>
              <Td>
                {customer.emailAddress ? customer.emailAddress.address : ''}
              </Td>
              <Td>{customer.mobilePhone ? customer.mobilePhone : customer.fixedPhone ? customer.fixedPhone : ''}</Td>
              <Td>{customer.mainAddress ? customer.mainAddress.fullName : ''}</Td>
              <Td>{customer.loyaltyCard ? customer.loyaltyCard.cardNumber : '_'}</Td>
              <Td>{customer.loyaltyCard ? customer.loyaltyCard.points : '_'}</Td>
              <Td>
                <MoreIcon
                  aria-controls="simple-menu"
                  aria-haspopup="true"
                  onClick={event => handleClick(event, customer.id)}
                />
              </Td>
              <Menu
                id="simple-menu"
                anchorEl={menuState.anchorEl}
                keepMounted
                open={menuState.customerId === customer.id}
                onClose={handleClose}
              >
                <MenuItem onClick={() => handleDetails(customer)}>
                  <PersonOutlineOutlinedIcon />
                  View Details
                </MenuItem>
                <MenuItem onClick={() => handleEdit(customer)}>
                  <ModeEditOutlineOutlinedIcon />
                  Edit
                </MenuItem>
                <MenuItem onClick={() => handleDelete(customer.id)}>
                  <DeleteOutlineOutlinedIcon />
                  Delete
                </MenuItem>
              </Menu>
            </TableRow>
          ))}
        </tbody>
      </CustomersTable>
      {showDeleteConfirm && (
          <DeleteConfirm onClose={handleCancelDelete} onConfirm={handleConfirmDelete} />
      )}
    </Container>
  );
};

export default CustomersList;
