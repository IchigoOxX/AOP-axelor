import React from "react";
import styled from "styled-components";
import Popup from "./Popup";

const ButtonContainer = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
`;

const ConfirmButton = styled.button`
  background-color: #0c085c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  margin-left: 10px;
`;

const CancelButton = styled.button`
  background-color: transparent;
  color: #0c085c;
  border: 1px solid #0c085c;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
`;

const DeleteConfirm = ({ onClose, onConfirm }) => {
    return (
        <Popup title="Confirm Delete" onClose={onClose}>
            <div>Are you sure you want to delete this customer?</div>
            <ButtonContainer>
                <CancelButton onClick={onClose}>Cancel</CancelButton>
                <ConfirmButton onClick={onConfirm}>Confirm</ConfirmButton>
            </ButtonContainer>
        </Popup>
    );
};

export default DeleteConfirm;
