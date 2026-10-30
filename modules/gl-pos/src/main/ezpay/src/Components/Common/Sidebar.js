import React, { useRef, useState } from "react";
import styled from "styled-components";
import HomeOutlinedIcon from "@mui/icons-material/HomeOutlined";
import ShoppingBagOutlinedIcon from "@mui/icons-material/ShoppingBagOutlined";
import WatchLaterOutlinedIcon from "@mui/icons-material/WatchLaterOutlined";
import FormatListBulletedOutlinedIcon from "@mui/icons-material/FormatListBulletedOutlined";
import PeopleAltOutlinedIcon from "@mui/icons-material/PeopleAltOutlined";
import SettingsOutlinedIcon from "@mui/icons-material/SettingsOutlined";
import { Link, useNavigate } from "react-router-dom";
import ArrowRightOutlinedIcon from "@mui/icons-material/ArrowRightOutlined";
import ArrowLeftOutlinedIcon from "@mui/icons-material/ArrowLeftOutlined";
import Logo from "./Logo";
import { useSelector } from "react-redux";
import SaveTicketConfirm from "../Orders/SaveTicketConfirm";
import { Alert, Fade } from "@mui/material";
const Container = styled.div`
  position: fixed;
  height: 100%;
  display: flex;
  flex-direction: row-reverse;
  align-items: center;
  z-index: 2;
`;

const ArrowIcon = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 12px;
  height: 50px;
  background-color: white;
  box-shadow: 2px 0 4px 0px rgba(0, 0, 0, 0.5);
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
`;
const SidebarContainer = styled.div`
  min-width: 220px;
  height: 100%;
  background-color: white;
  box-shadow: 2px 0 4px 0px rgba(0, 0, 0, 0.25);
  border-top-right-radius: 20px;
  border-bottom-right-radius: 20px;
  box-sizing: border-box;
`;
const Menus = styled.div`
  padding-top: 40px;
  display: flex;
  flex-direction: column;
  row-gap: 5px;
`;
const Menu = styled(Link)`
  padding: 7px 10px 7px 30px;
  display: flex;
  align-items: center;
  column-gap: 15px;
  color: #8d91a0;
  text-decoration: none;
  cursor: pointer;
  &:hover {
    background-color: rgba(141, 145, 160, 0.25);
  }
  & > * {
    margin: 0;
    padding: 0px;
    font-weight: 500;
  }
`;
const AlertNotif = styled(Alert)`
  position: absolute;
  top: 10px;
  right: 45%;
  z-index: 10;
`;


const Sidebar = (props) => {
  const [showSave, setShowSave] = useState(false);
  const [nextPath, setNextPath] = useState("");
  const [saved, setSaved] = useState(false);
  const cartItems = useSelector((state) => state.cart.cartItems);
  const navigate = useNavigate();

  const handleSidebar = () => {
    props.setShowSidebar(!props.showSidebar);
  };

  const sidebarRef = useRef(null);
  const handleClickOutside = (event) => {
    if (sidebarRef.current && !sidebarRef.current.contains(event.target)) {
      props.setShowSidebar(false);
    }
  };
  document.addEventListener("mousedown", handleClickOutside);

  const handleNavigation = (path) => {
    if (cartItems.length > 0) {
      setNextPath(path);
      setShowSave(true);
    } else {
      navigate(path);
    }
  };

  const confirmNavigation = () => {
    setShowSave(false);
    navigate(nextPath);
  };

  return (
    <>
      <Container>
        <ArrowIcon onClick={handleSidebar}>
          {props.showSidebar ? (
            <ArrowLeftOutlinedIcon />
          ) : (
            <ArrowRightOutlinedIcon />
          )}
        </ArrowIcon>
        {props.showSidebar && (
          <SidebarContainer ref={sidebarRef}>
            <Logo />
            <Menus>
              <Menu to={"/"}>
                <HomeOutlinedIcon />
                <h4>Home</h4>
              </Menu>
              <Menu as="div" onClick={() => handleNavigation("/orders")}>
                <ShoppingBagOutlinedIcon />
                <h4>Orders</h4>
              </Menu>
              <Menu as="div" onClick={() => handleNavigation("/shifts")}>
                <WatchLaterOutlinedIcon />
                <h4>Shifts</h4>
              </Menu>
              <Menu as="div" onClick={() => handleNavigation("/items")}>
                <FormatListBulletedOutlinedIcon />
                <h4>Items</h4>
              </Menu>
              <Menu as="div" onClick={() => handleNavigation("/customers")}>
                <PeopleAltOutlinedIcon />
                <h4>Customers</h4>
              </Menu>
              <Menu as="div" onClick={() => handleNavigation("/settings")}>
                <SettingsOutlinedIcon />
                <h4>Settings</h4>
              </Menu>
            </Menus>
          </SidebarContainer>
        )}
      </Container>
      {showSave && (
        <SaveTicketConfirm
          setShowSave={setShowSave}
          confirmNavigation={confirmNavigation}
          setSaved={setSaved}
        />
      )}
      <Fade in={saved}>
        <AlertNotif severity="success">The ticket is saved.</AlertNotif>
      </Fade>
    </>
  );
};

export default Sidebar;
