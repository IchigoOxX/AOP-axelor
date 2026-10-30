import React from "react";
import styled from "styled-components";
import cart from "../../assets/cart.png";
import { Link } from "react-router-dom";

const Container = styled(Link)`
  display: flex;
  gap: 5px;
  align-items: center;
  text-decoration: none;
  color: black;
  cursor: pointer;
`;
const Img = styled.img`
  max-width: 100px;
`;

const Logo = () => {
  return (
    <Container to="/">
      <Img src={cart} alt="" />
      <h1>EZPay</h1>
    </Container>
  );
};

export default Logo;
