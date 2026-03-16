import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    int time;

    Transaction(int id,int amount,String merchant,String account,int time){
        this.id=id;
        this.amount=amount;
        this.merchant=merchant;
        this.account=account;
        this.time=time;
    }
}

public class Q9 {

    public static List<int[]> findTwoSum(List<Transaction> tx,int target){
        Map<Integer,Transaction> map=new HashMap<>();
        List<int[]> res=new ArrayList<>();

        for(Transaction t:tx){
            int complement=target-t.amount;
            if(map.containsKey(complement)){
                res.add(new int[]{map.get(complement).id,t.id});
            }
            map.put(t.amount,t);
        }
        return res;
    }

    public static List<int[]> findTwoSumTimeWindow(List<Transaction> tx,int target,int window){
        Map<Integer,Transaction> map=new HashMap<>();
        List<int[]> res=new ArrayList<>();

        for(Transaction t:tx){
            int complement=target-t.amount;

            if(map.containsKey(complement)){
                Transaction prev=map.get(complement);
                if(Math.abs(t.time-prev.time)<=window){
                    res.add(new int[]{prev.id,t.id});
                }
            }

            map.put(t.amount,t);
        }

        return res;
    }

    public static List<List<Integer>> findKSum(List<Transaction> tx,int k,int target){
        List<List<Integer>> res=new ArrayList<>();
        int n=tx.size();
        int[] amounts=new int[n];
        int[] ids=new int[n];

        for(int i=0;i<n;i++){
            amounts[i]=tx.get(i).amount;
            ids[i]=tx.get(i).id;
        }

        Arrays.sort(amounts);
        kSumHelper(amounts,ids,0,k,target,new ArrayList<>(),res);
        return res;
    }

    private static void kSumHelper(int[] nums,int[] ids,int start,int k,int target,List<Integer> path,List<List<Integer>> res){
        if(k==2){
            Map<Integer,Integer> map=new HashMap<>();

            for(int i=start;i<nums.length;i++){
                int complement=target-nums[i];
                if(map.containsKey(complement)){
                    List<Integer> list=new ArrayList<>(path);
                    list.add(ids[map.get(complement)]);
                    list.add(ids[i]);
                    res.add(list);
                }
                map.put(nums[i],i);
            }
            return;
        }

        for(int i=start;i<nums.length;i++){
            path.add(ids[i]);
            kSumHelper(nums,ids,i+1,k-1,target-nums[i],path,res);
            path.remove(path.size()-1);
        }
    }

    public static List<String> detectDuplicates(List<Transaction> tx){
        Map<String,Set<String>> map=new HashMap<>();
        List<String> res=new ArrayList<>();

        for(Transaction t:tx){
            String key=t.amount+"-"+t.merchant;

            map.putIfAbsent(key,new HashSet<>());
            map.get(key).add(t.account);

            if(map.get(key).size()>1){
                res.add(key+" "+map.get(key));
            }
        }

        return res;
    }

    public static void main(String[] args){

        List<Transaction> transactions=new ArrayList<>();

        transactions.add(new Transaction(1,500,"StoreA","acc1",600));
        transactions.add(new Transaction(2,300,"StoreB","acc2",615));
        transactions.add(new Transaction(3,200,"StoreC","acc3",630));
        transactions.add(new Transaction(4,500,"StoreA","acc4",640));

        System.out.println(findTwoSum(transactions,500));

        System.out.println(findTwoSumTimeWindow(transactions,500,60));

        System.out.println(findKSum(transactions,3,1000));

        System.out.println(detectDuplicates(transactions));
    }
}