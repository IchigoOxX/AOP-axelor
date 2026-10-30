import React, { useRef, useState } from 'react';
import styled from 'styled-components';
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import ButtonComponent from '../Common/ButtonComponent';

const Container = styled.div`
  position: relative;
`;

const DropdownMenu = styled.div`
  position: absolute;
  top: 35px;
  right: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 10px 0px;
  width: max-content;
`;

const DropdownItem = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  cursor: pointer;
  background-color: ${props => (props.active ? '#a8d0e740' : 'white')};
  &:hover {
    background-color: #f1f1f1;
  }
`;

const FilterComponent = props => {
  const [isOpen, setIsOpen] = useState(false);
  const filterRef = useRef(null);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  const handleCheckboxChange = event => {
    const { name, checked } = event.target;

    // If "Active Customers" is clicked, deselect "Inactive Customers"
    if (name === 'activeCustomers' && checked) {
      props.setFilters(prevFilters => ({
        ...prevFilters,
        activeCustomers: checked,
        inactiveCustomers: false,
      }));
    }
    // If "Inactive Customers" is clicked, deselect "Active Customers"
    else if (name === 'inactiveCustomers' && checked) {
      props.setFilters(prevFilters => ({
        ...prevFilters,
        activeCustomers: false,
        inactiveCustomers: checked,
      }));
    }
    // Otherwise, toggle the checkbox as usual
    else {
      props.setFilters(prevFilters => ({
        ...prevFilters,
        [name]: checked,
      }));
    }
  };

  const handleClickOutside = event => {
    if (filterRef.current && !filterRef.current.contains(event.target)) {
      setIsOpen(false);
    }
  };

  React.useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
    <Container ref={filterRef}>
      <ButtonComponent title="Filter" icon={<FilterAltOutlinedIcon fontSize="sm" />} handleClick={toggleDropdown} />
      {isOpen && (
        <DropdownMenu>
          <DropdownItem>
            <input
              type="checkbox"
              name="mostPurchases"
              checked={props.filters.mostPurchases}
              onChange={handleCheckboxChange}
            />
            Most Purchases
          </DropdownItem>
          <DropdownItem>
            <input
              type="checkbox"
              name="activeCustomers"
              checked={props.filters.activeCustomers}
              onChange={handleCheckboxChange}
            />
            Active Customers
          </DropdownItem>
          <DropdownItem>
            <input
              type="checkbox"
              name="inactiveCustomers"
              checked={props.filters.inactiveCustomers}
              onChange={handleCheckboxChange}
            />
            Inactive Customers
          </DropdownItem>
        </DropdownMenu>
      )}
    </Container>
  );
};

export default FilterComponent;
