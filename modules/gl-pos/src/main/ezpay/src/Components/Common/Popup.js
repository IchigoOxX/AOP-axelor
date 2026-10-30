import React from "react";
import styled from "styled-components";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";

const Container = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  color: black;
  z-index: 1;
`;
const Box = styled.div`
  /* position: relative; */
  background-color: white;
  width: fit-content;
  border: solid #ccc 1px;
  border-radius: 8px;
  box-shadow: 0px 4px 20px rgba(0, 0, 0, 0.1);
  padding: 20px;
`;
const Header = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #0c085c;
`;
const Title = styled.div`
  margin: auto;
  padding: 0 15px;
  font-size: 22px;
  font-weight: bold;
`;
const Icon = styled.div`
  cursor: pointer;
  transition: color 0.3s ease;
  padding-top: 5px;
  &:hover {
    color: rgba(0, 0, 0, 0.75);
  }
`;
const Popup = (props) => {
  return (
    <Container>
      <Box>
        <Header>
          <Title>{props.title}</Title>
          <Icon onClick={props.onClose}>
            <CloseOutlinedIcon />
          </Icon>
        </Header>
        {props.children}
      </Box>
    </Container>
  );
};

export default Popup;