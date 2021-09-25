import { ref, watch, Ref } from 'vue';
import { User, BTFType, OrderTypeFilter, OrderType, OrderTypeList } from 'components/models';
import useBankConnectionsAPI from './bankconnections';
import useFileTransferAPI from './filetransfer';

//Global cache of all active BTF types for all active bank connections..
const activeTypes: Ref<Map<number,OrderTypeList>> = ref<Map<number,OrderTypeList>>(new Map());

export default function useOrderTypesAPI(selectedBC:Ref<User | undefined>, filterType:Ref<OrderTypeFilter>) {
  //BTF types for given selectedBC
  const btfTypes: Ref<BTFType[]> = ref([]);
  const orderTypes: Ref<OrderType[]> = ref([]);

  const { ebicsOrderTypes } = useFileTransferAPI();
  const { activeBankConnections } = useBankConnectionsAPI();

  /**
   * Update global BTF types cache
   */
  const updateBtfTypesForActiveConnections = async():Promise<void> => {
    if (activeBankConnections.value) {
      for (const bankConnection of activeBankConnections.value) {
        let orderTypeList:OrderTypeList | undefined = activeTypes.value.get(bankConnection.id);
        //If not fetched yet, lets fetch order types for all version into cache
        if (!orderTypeList) {
          orderTypeList = {btfTypes: [], orderTypes: []} as OrderTypeList
          activeTypes.value.set(bankConnection.id, orderTypeList)
        }
          
        if (orderTypeList && !orderTypeList.btfTypes.length) { 
          try {
            orderTypeList.btfTypes = await ebicsOrderTypes(bankConnection, 'H005');
            console.log(`BtfTypes loaded for ${bankConnection.name}, types: ${JSON.stringify(orderTypeList.btfTypes)}`)
            refreshSelectedOrderTypes(bankConnection);
          } catch (error) {}
        }

        if (orderTypeList && !orderTypeList.orderTypes.length) { 
          try {
            orderTypeList.orderTypes = await ebicsOrderTypes(bankConnection, 'H004') as OrderType[];
            console.log(`Order types loaded for ${bankConnection.name}, types: ${JSON.stringify(orderTypeList.orderTypes)}`)
            refreshSelectedOrderTypes(bankConnection);
          } catch (error) {}
        }
      }
    }
  };

  const refreshSelectedOrderTypes = (bankConnection: User): void => {
    //Trigger refresh of filtered list after retreiving server response
    if (selectedBC.value?.id == bankConnection.id) { 
      selectUserOrderTypes();
    }
  };

  /**
   * Choose proper btfTypes base on user selected bank connection
   * Filter the result filterType
   */
  const selectUserOrderTypes = () => {
    if (selectedBC.value) {
      const selectedTypes = activeTypes.value.get(selectedBC.value.id);
      if (selectedTypes) {
        console.log('Refreshing order type list for selected BC: ' + selectedBC.value.name)
        if (filterType.value == OrderTypeFilter.All) {
          btfTypes.value = selectedTypes.btfTypes;
          orderTypes.value = selectedTypes.orderTypes;
        } else if (filterType.value == OrderTypeFilter.UploadOnly) {
          btfTypes.value = selectedTypes.btfTypes.filter(btf => btf.adminOrderType == 'BTU')
          orderTypes.value = selectedTypes.orderTypes.filter(ot => ot.adminOrderType == 'UPL' || ot.adminOrderType == 'FUL');
        } else if (filterType.value == OrderTypeFilter.DownloadOnly) {
          btfTypes.value = selectedTypes.btfTypes.filter(btf => btf.adminOrderType == 'BTD' || btf.adminOrderType == 'HAC')
          orderTypes.value = selectedTypes.orderTypes.filter(ot => ot.adminOrderType == 'DNL' || ot.adminOrderType == 'FDL' || ot.adminOrderType == 'HAC');
        }
      } else {
        console.error('No BTF types / OrderTypes loaded for given bank connection: ' + selectedBC.value.id.toString())
      }
    }
  }
  
  function s(input: string | undefined): string {
    if (input) return input;
    else return '-';
  }

  const orderTypeLabel = (ot: OrderType | undefined): string => {
    if (ot) {
      return ot.orderType
    } else 
      return '';
  };

  const btfTypeLabel = (btf: BTFType | undefined): string => {
    if (btf) {
      const bts = btf.service;
      const btm = bts?.message;
      return `${s(bts?.serviceName)}|${s(bts?.serviceOption)}|${s(
        bts?.scope
      )}|${s(bts?.containerType)}|${s(btm?.messageName)}|${s(
        btm?.messageNameVariant
      )}|${s(btm?.messageNameVersion)}|${s(btm?.messageNameFormat)}`;
    } else return '';
  };
/** 
  function extensionFromDescription(description: string | undefined): string | undefined {
    if (description?.includes('ZIP') || description?.includes('zip'))
      return 'zip';
    else if (description?.includes('XML') || description?.includes('xml'))
      return 'xml';
    else if (description?.includes('csv') || description?.includes('csv'))
      return 'csv';  
    return undefined;
  }

  
  const getDownloadFileNameFromOrderType = (ot: OrderType):string => {
    return `${ot.adminOrderType}`
  }

  const getDownloadFileExtensionFromOrderType = (ot: OrderType):string | undefined => {
    return extensionFromDescription(ot.description);
  }

  const getDownloadFileNameFromBtfType = (btf: BTFType):string => {
    if (btf.service) {
      return `${btf.service.serviceName}-${btf.service.message.messageName}`
    } else
      return btf.adminOrderType
  }

  const getDownloadFileExtensionFromBtfType = (btf: BTFType):string | undefined => {
    if (btf.service) {
      switch(btf.service.containerType) {
        case 'ZIP':
          return 'zip';
          case 'XML':
            return 'xml';
        default:
          return extensionFromDescription(btf.description);
      }
    } else
      return undefined
  } **/

  watch(selectedBC, selectUserOrderTypes);
  watch(activeBankConnections, updateBtfTypesForActiveConnections);

  return {
    btfTypes, orderTypes, btfTypeLabel, orderTypeLabel,
  };
}
