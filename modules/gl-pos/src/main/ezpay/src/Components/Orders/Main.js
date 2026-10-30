import React, { useState } from "react";
import styled from "styled-components";
import ButtonComponent from "../Common/ButtonComponent";
import QrCodeScannerOutlinedIcon from "@mui/icons-material/QrCodeScannerOutlined";
import OrdersList from "./OrdersList";
import LocalPrintshopOutlinedIcon from "@mui/icons-material/LocalPrintshopOutlined";
import Scanning from "../Common/Scanning";
import PrintReceipt from "./PrintReceipt";

const Container = styled.div`
  margin: 0 30px;
`;
const Header = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
`;
const H2 = styled.h2`
  font-size: 27px;
  color: #0c085c;
`;
const Buttons = styled.div`
  display: flex;
  gap: 7px;
`;

const Main = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [isScanOpen, setIsScanOpen] = useState(false);
  const [selectedTickets, setSelectedTickets] = useState([]);
  const [printPopup, setPrintPopup] = useState(false);

  const openScan = () => {
    setIsScanOpen(true);
  };
  const closeScan = () => {
    setIsScanOpen(false);
  };
  const handlePrintSelected = () => {
    setPrintPopup(true)
  };
  const handleAfterPrint = () => {
    setPrintPopup(false);
  };

  return (
    <Container>
      <Header>
        <H2>Orders List</H2>
        <Buttons>
          <ButtonComponent
            title="Print"
            icon={<LocalPrintshopOutlinedIcon fontSize="sm" />}
            handleClick={handlePrintSelected}
          />
          <ButtonComponent
            title="Scan Code"
            icon={<QrCodeScannerOutlinedIcon fontSize="sm" />}
            handleClick={openScan}
          />
          {isScanOpen && <Scanning handleClose={closeScan} />}
        </Buttons>
      </Header>
      <OrdersList
        selectedTickets={selectedTickets}
        setSelectedTickets={setSelectedTickets}
      />
      {printPopup && (
        <PrintReceipt
          tickets={selectedTickets}
          handlePrint={handleAfterPrint}
          handlePrintCancel={handleAfterPrint}
        />
      )}
    </Container>
  );
};

export default Main;
