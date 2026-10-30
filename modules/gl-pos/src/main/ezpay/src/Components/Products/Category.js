import React from "react";
import styled from "styled-components";

const Button = styled.button`
  border-radius: 10px;
  background-color: white;
  margin: 10px 8px 10px 0;
  font-size: 16px;
  color: ${(props) => (props.active ? "#A8D0E7" : "#000000")};
  border: ${(props) => (props.active ? "#A8D0E7 solid 2px" : "lightgray solid 1px")};
  box-shadow: ${(props) => (props.active ? "0px 4px 4px 0px rgba(0, 0, 0, 0.25)": "0px 2px 2px 0px rgba(0, 0, 0, 0.25)")};
  cursor: pointer;
`;

const Category = ({ name, active, onClick }) => {
  return (
    <Button active={active} onClick={onClick}>
      {name}
    </Button>
  );
};

export default Category;