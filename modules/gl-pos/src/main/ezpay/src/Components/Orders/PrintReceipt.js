import React, { useRef } from "react";
import Popup from "../Common/Popup";
import ReactToPrint from "react-to-print";
import styled from "styled-components";
import Receipt from "./Receipt";

const ReceiptContainer = styled.div`
    max-height: calc(100vh - 200px);
    overflow-y: auto;
`
const Buttons = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 10px;
`;
const Button = styled.button`
  flex: 1;
  padding: 10px;
  font-size: 18px;
  font-weight: bold;
  color: #0c085c;
  background-color: #a8d0e7;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
  transition: color 0.3s;
  box-shadow: 0 1px 2px 1px rgba(0, 0, 0, 0.4);
  &:hover {
    background-color: #54528c;
    color: #a8d0e7;
  }
`;

const PrintReceipt = ({ tickets, handlePrint, handleDontPrint, handlePrintCancel }) => {
  const receiptRef = useRef();

  return (
    <Popup title="Print Receipt" onClose={handlePrintCancel}>
        <>
        <ReceiptContainer ref={receiptRef}>
      {tickets.map((ticket) => (
            <Receipt ticket={ticket} />
        ))}
        </ReceiptContainer>
          <Buttons>
            <ReactToPrint
              trigger={() => <Button>Yes</Button>}
              content={() => receiptRef.current}
              onAfterPrint={handlePrint}
            />
            <Button onClick={handlePrint}>No</Button>
          </Buttons>
        </>
    </Popup>
  );
};

export default PrintReceipt;
