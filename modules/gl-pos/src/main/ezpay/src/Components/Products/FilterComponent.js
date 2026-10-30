// Import necessary packages
import React, { useRef, useState } from 'react';
import styled from 'styled-components';
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import SwapVertOutlinedIcon from '@mui/icons-material/SwapVertOutlined';

const Container = styled.div`
  position: relative;
`;
const Button = styled.button`
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 20px;
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.1);
  padding: 4px 8px;
  font-size: 17px;
  cursor: pointer;
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
  z-index: 1;
`;

const DropdownItem = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  cursor: pointer;
  background-color: ${props => (props.active ? '#a8d0e740' : 'transparent')};
  &:hover {
    background-color: ${props => (props.active ? '#a8d0e740' : '#f5f5f5')};
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
    props.setFilters(prevFilters => ({
      ...prevFilters,
      [name]: checked,
    }));
  };

  const handleSortChange = order => {
    props.setSortOrder(prevOrder => (prevOrder === order ? null : order));
    setIsOpen(false);
  };

  const handleClickOutside = event => {
    if (filterRef.current && !filterRef.current.contains(event.target)) {
      setIsOpen(false);
    }
  };
  document.addEventListener('mousedown', handleClickOutside);

  return (
    <Container ref={filterRef}>
      <Button onClick={toggleDropdown}>
        Filter
        <FilterAltOutlinedIcon fontSize="sm" />
      </Button>
      {isOpen && (
        <DropdownMenu>
          <DropdownItem>
            <input
              type="checkbox"
              name="newArrivals"
              checked={props.filters.newArrivals}
              onChange={handleCheckboxChange}
            />
            New Arrivals
          </DropdownItem>
          <DropdownItem>
            <input
              type="checkbox"
              name="onPromotion"
              checked={props.filters.onPromotion}
              onChange={handleCheckboxChange}
            />
            On Promotion
          </DropdownItem>
          <DropdownItem>
            <input
              type="checkbox"
              name="bestSellers"
              checked={props.filters.bestSellers}
              onChange={handleCheckboxChange}
            />
            Best Sellers
          </DropdownItem>
          <DropdownItem>
            <input type="checkbox" name="inStock" checked={props.filters.inStock} onChange={handleCheckboxChange} />
            In Stock
          </DropdownItem>
          <DropdownItem active={props.sortOrder === 'asc'} onClick={() => handleSortChange('asc')}>
            <SwapVertOutlinedIcon fontSize="sm" />
            Price: Low to High
          </DropdownItem>
          <DropdownItem active={props.sortOrder === 'desc'} onClick={() => handleSortChange('desc')}>
            <SwapVertOutlinedIcon fontSize="sm" />
            Price: High to Low
          </DropdownItem>
        </DropdownMenu>
      )}
    </Container>
  );
};

export default FilterComponent;
