import React, { useState } from "react";
import Search from "./Search";
import styled from "styled-components";
import Register from "./Register";
import Sidebar from "../Common/Sidebar";
import Logo from "./Logo";

const Container = styled.div`
  display: flex;
  justify-content: space-between;
`;
const Div = styled.div`
  margin: 0 15px;
  display: flex;
  gap: 5px;
  align-items: center;
  cursor: pointer;
`;

const Header = () => {
  return (
    <Container>
      <Logo />
      <Div>
        <Search />
        <Register />
      </Div>
    </Container>
  );
};

export default Header;
