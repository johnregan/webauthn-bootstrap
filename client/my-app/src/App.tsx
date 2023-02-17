import React, { useState } from "react";

function App() {
  const [message, setMessage] = useState("");

  const startRegistration = async () => {
    const response = await fetch("http://localhost:8080/startRegistration", {
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
    const data = await response.text();
    setMessage(data);
  };

  const endRegistration = async () => {
    const response = await fetch("http://localhost:8080/endRegistration", {
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
    const data = await response.text();
    setMessage(data);
  };

  return (
    <div>
      <button onClick={startRegistration}>Start Registration</button>
      <button onClick={endRegistration}>End Registration</button>
      <p>{message}</p>
    </div>
  );
}

export default App;
