import axios, { AxiosResponse } from 'axios';
import { encode, decode } from 'base64-arraybuffer';
import { useState } from "react";


const publicKeyCredentialToJSON = (pubKeyCred: any): any => {
  if (pubKeyCred instanceof ArrayBuffer) {
    return encode(pubKeyCred);
  } else if (pubKeyCred instanceof Array) {
    return pubKeyCred.map(publicKeyCredentialToJSON);
  } else if (pubKeyCred instanceof Object) {
    const obj: any = {};
    for (let key in pubKeyCred) {
      obj[key] = publicKeyCredentialToJSON(pubKeyCred[key]);
    }
    return obj;
  } else return pubKeyCred;
};

const useNewBiometricAuth = () => {
  navigator.credentials.preventSilentAccess();
  const challengeString = '6ed375d10e264bd9752ff718';

  // 2.
  const getChallenge = () => {
    return decode(challengeString);
  };

  // 3.
  const createPublickey = async (challenge: ArrayBuffer) => {
    const user = {
      id: 123,
      email: 'dqtkien@gmail.com',
      displayName: 'Kevin ABC',
    };
    const attestation = await navigator.credentials.create({
      publicKey: {
        authenticatorSelection: {
          authenticatorAttachment: 'platform',
          userVerification: 'required',
        },
        timeout: 1800000,

        challenge,
        rp: { id: window.location.hostname, name: 'My Test Hello PWA' },
        user: {
          id: decode(user.id.toString()),
          name: user.email,
          displayName: user.displayName,
        },
        pubKeyCredParams: [
          { type: 'public-key', alg: -7 },
          { type: 'public-key', alg: -257 },
        ],
      },
    });

    const webAuthnAttestation = publicKeyCredentialToJSON(attestation);
    return webAuthnAttestation;
  };

  // 4.
  const verifyAndStorePublicKey = async (publicKey: any) => {
    const result = await axios.post(
      'http://localhost:4000/v1/validate-and-store-pbkey',
      {
        publicKey,
      }
    );
    console.log(result);
  };

  return {
    getChallenge,
    createPublickey,
    verifyAndStorePublicKey,
  };
};

class AuthenticatorData {
  rawId: string;
  response: {
    attestationObject: string;
    clientDataJSON: string;
  };
  authenticatorAttachment: string;
  id: string;
  type: string;
  registerToken: string
}

function App() {
  const [jwt, setJwt] = useState("");
  const { createPublickey, getChallenge } = useNewBiometricAuth();

  const registerBio = async () => {
    const challenge = getChallenge();
    const publicKeyResult = await createPublickey(challenge);
    console.log('register', publicKeyResult);
    localStorage.setItem('credId', publicKeyResult.id);
    console.log('credId= result.id', publicKeyResult.id);
    if (window.PublicKeyCredential) {
      PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable().then(
        (uvpaa) => {
          if (uvpaa) {
            console.log('system support');
          } else {
            console.log('not');
          }
        }
      );
    } else {
      console.log('not');
    }
    const rawJson = JSON.parse(JSON.stringify(publicKeyResult));

    const authenticatorData = new AuthenticatorData();
    authenticatorData.rawId = rawJson.rawId;
    authenticatorData.response = { attestationObject: rawJson.response.attestationObject, clientDataJSON: rawJson.response.clientDataJSON };
    authenticatorData.authenticatorAttachment = rawJson.authenticatorAttachment;
    authenticatorData.id = rawJson.id;
    authenticatorData.type = rawJson.type;
    authenticatorData.registerToken = JSON.parse(jwt).registerToken;

    return authenticatorData;
  };

  const startRegistration = async () => {

    const response = await fetch("http://localhost:8080/startRegistration", {
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
    const data = await response.text();
    setJwt(data);
  };

  const endRegistration = async () => {
    const publicKeyResult = await registerBio();

    const config = {

      headers: {

        'Access-Control-Allow-Origin': '*', // Allow requests from any origin
        'Access-Control-Allow-Headers': 'X-Requested-With, Content-Type, Authorization', // Allow these headers
      }
    };

    const apiUrl = "http://localhost:8080/endRegistration";

    const postRepsonse = axios.post(apiUrl, publicKeyResult, config)
      .then((response) => {
        console.log(response.data);
        return response.data; // Returns the response data
      })
      .catch((error) => {
        console.error(error);
      });
    const data = await postRepsonse;
    setJwt(data);
  };

  return (
    <div>
      <button onClick={startRegistration}>Start Registration</button>
      <button onClick={endRegistration}>End Registration</button>
      <p>{jwt}</p>
    </div>
  );
}



export default App;
