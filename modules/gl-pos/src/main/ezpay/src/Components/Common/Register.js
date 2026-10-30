import React, { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import styled from 'styled-components';
import { fetchUsersThunk, selectUser} from '../Redux/usersSlice';

const Container = styled.div`
  position: relative;
  display: flex;
  justify-content: center;
  //min-width: 80px;
  border: solid black 1px;
  border-radius: 65px;
  height: 25px;
  //margin-right: 12px;
  padding: 5px 15px;
  box-shadow: 0px 3px 3px 0px rgba(0, 0, 0, 0.25);
  cursor: pointer;
`;

const DropdownContent = styled.div`
  display: ${(props) => (props.open ? 'block' : 'none')};
  position: absolute;
  top: 30px;
  right: 0;
  max-height: 50vh; /* Adjust height to be 50% of viewport height */
  overflow-y: auto; /* Enable scrolling */
  background-color: white;
  min-width: 98%;
  border: solid black 1px;
  border-radius: 10px;
  box-shadow: 0px 3px 3px 0px rgba(0, 0, 0, 0.25);
  z-index: 10;
`;

const DropdownItem = styled.div`
  padding: 4px 8px;
  background-color: ${(props) => (props.selected ? '#a8d0e740' : 'transparent')}; /* Highlight selected user */
  &:hover {
    background-color: #f1f1f1;
    cursor: pointer;
  }
`;

const Register = () => {
  const [isOpen, setIsOpen] = useState(false);
  const registerRef = useRef(null);
  const dispatch = useDispatch();
  const users = useSelector((state) => state.users.usersList);
  const currentUser = useSelector((state) => state.users.currentUser);

  useEffect(() => {
    dispatch(fetchUsersThunk());
  }, [dispatch]);

  useEffect(() => {
    // Scroll to selected user when dropdown opens
    if (isOpen && currentUser) {
      const selectedUserElement = registerRef.current.querySelector(`.user-item-${currentUser.id}`);
      if (selectedUserElement) {
        selectedUserElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start',
        });
      }
    }
  }, [isOpen, currentUser]);
  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  const handleOptionClick = (user) => {
    dispatch(selectUser(user));
    setIsOpen(false);
  };

  const handleClickOutside = (event) => {
    if (registerRef.current && !registerRef.current.contains(event.target)) {
      setIsOpen(false);
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  return (
      <Container onClick={toggleDropdown} ref={registerRef}>
        {currentUser ? currentUser.name : 'Select User'}
        {/*<KeyboardArrowDownIcon />*/}
        {/*<DropdownContent open={isOpen}>*/}
        {/*  {users.map((user) => (*/}
        {/*      <DropdownItem key={user.id} onClick={() => handleOptionClick(user)} selected={currentUser && currentUser.id === user.id} // Highlight selected user*/}
        {/*                    className={`user-item-${user.id}`} >*/}
        {/*        {user.name}*/}
        {/*      </DropdownItem>*/}
        {/*  ))}*/}
        {/*</DropdownContent>*/}
      </Container>
  );
};

export default Register;
