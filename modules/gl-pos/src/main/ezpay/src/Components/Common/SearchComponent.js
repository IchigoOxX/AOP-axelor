import React, { useState } from "react";
import styled from "styled-components";
import SearchOutlinedIcon from "@mui/icons-material/SearchOutlined";

const Container = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: white;
  border: 1px solid #ccc;
  border-radius: 20px;
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.1);
  padding: 2px 8px;
  cursor: pointer;
`;
const Input = styled.input`
  margin: 0;
  padding: 2px 4px;
  border: none;
  outline: none;
  font-size: 17px;
  width: ${(props) => (props.expanded ? "110px" : "55px")};
  &::placeholder {
    color: ${(props) => (props.expanded ? "gray" : "black")};
  }
`;

const SearchComponent = (props) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [query, setQuery] = useState("");

  const openSearch = () => {
    setIsExpanded(true);
  };

  const closeSearch = () => {
    setIsExpanded(false);
  };

  const handleChange = (e) => {
    const value = e.target.value;
    setQuery(value);
    props.setSearchQuery(value);
  };

  return (
    <Container>
      <Input
        type="text"
        placeholder="Search"
        onFocus={openSearch}
        onBlur={closeSearch}
        expanded={isExpanded}
        value={query}
        onChange={handleChange}
      />
      <div onClick={openSearch}>
        <SearchOutlinedIcon fontSize="sm" />
      </div>
    </Container>
  );
};

export default SearchComponent;
