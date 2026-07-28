import {apiFetch, readJsonBody} from "../http";

export type Customer = {
    id: string;
    name: string;
};

type CustomersResponseBody = {
    content?: Customer[];
};

export type FetchCustomersWithSessionResult =
    | { ok: true; customers: Customer[] }
    | { ok: false; status: number };

export async function fetchCustomersWithSession(): Promise<FetchCustomersWithSessionResult> {
    const response = await apiFetch("/api/v1/customer", {
        method: "GET",
        headers: {
            Accept: "application/json",
        },
    });

    if (!response.ok) {
        return {ok: false, status: response.status};
    }

    const body = await readJsonBody<CustomersResponseBody>(response);
    return {ok: true, customers: body.content ?? []};
}
