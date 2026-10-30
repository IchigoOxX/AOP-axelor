import React, { useEffect, useMemo, useState } from 'react';
import styled from 'styled-components';
import SearchIcon from '@mui/icons-material/Search';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Mode } from '@mui/icons-material';

const Container = styled.div`
  position: relative;
  display: flex;
  width: 330px;
  justify-content: space-between;
  align-items: center;
  border: solid black 1px;
  border-radius: 65px;
  height: 25px;
  padding: 5px 15px;
  box-shadow: 0px 3px 3px 0px rgba(0, 0, 0, 0.25);
`;
const Input = styled.input`
  width: 100%;
  height: 100%;
  margin-right: 5px;
  border: none;
  outline: none;
  font-size: 16px;
`;
const SuggestionsList = styled.ul`
  position: absolute;
  top: 35px;
  width: 100%;
  border: 1px solid #ddd;
  background-color: #fff;
  z-index: 20;
  list-style: none;
  padding: 0;
  margin: 0;
  box-shadow: 0px 3px 3px 0px rgba(0, 0, 0, 0.25);
`;

const SuggestionItem = styled.li`
  padding: 10px;
  cursor: pointer;
  &:hover {
    background-color: #f0f0f0;
  }
`;

const Search = () => {
  const [searchValue, setSearchValue] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const customers = useSelector(state => state.customers.customersList);
  const products = useSelector(state => state.products.products.list);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const searchableItems = useMemo(
    () => [
      ...customers.map(customer => ({
        id: customer.id,
        type: 'customer',
        name: customer.simpleFullName,
        firstName: customer.firstName,
        nic: customer.nic,
      })),
      ...products.map(product => ({
        id: product.id,
        type: 'product',
        name: product.name,
      })),
      // Add more items as needed
    ],
    [customers, products],
  );

  useEffect(() => {
    if (searchValue) {
      const lowercasedFilter = searchValue.toLowerCase();
      const filteredItems = searchableItems.filter(
        item =>
          item.name.toLowerCase().includes(lowercasedFilter) ||
          (item.firstName && item.firstName.toLowerCase().includes(lowercasedFilter)) ||
          (item.nic && item.nic.toLowerCase().includes(lowercasedFilter)),
      );

      setSuggestions(filteredItems);
    } else {
      setSuggestions([]);
    }
  }, [searchValue, searchableItems]);

  const handleChange = event => {
    setSearchValue(event.target.value);
  };

  const handleSearch = event => {
    setSearchValue(event.target.value);
  };

  const handleKeyPress = event => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };
  const handleSuggestionClick = suggestion => {
    if (suggestion.type === 'customer') {
      navigate(`/customers/customer-details`, {
        state: { customer: suggestion, mode: 'view' },
      });
    } else if (suggestion.type === 'product') {
      navigate(`/items/item-details`, {
        state: { product: suggestion },
      });
    }
    setSuggestions([]);
  };
  return (
    <Container>
      <Input type="text" placeholder="SEARCH" value={searchValue} onChange={handleChange} onKeyPress={handleKeyPress} />
      <SearchIcon onClick={handleSearch} />
      {suggestions.length > 0 && (
        <SuggestionsList>
          {suggestions.map(suggestion => (
            <SuggestionItem key={suggestion.id} onClick={() => handleSuggestionClick(suggestion)}>
              {suggestion.name}
            </SuggestionItem>
          ))}
        </SuggestionsList>
      )}
    </Container>
  );
};

export default Search;
